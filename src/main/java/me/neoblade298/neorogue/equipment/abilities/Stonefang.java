package me.neoblade298.neorogue.equipment.abilities;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

// Custom ground-hugging ability: after a 1s charge, resolves a chain of up to 3 strike points at
// 3/6/9 blocks along the player's horizontal facing. Each point snaps to the ground surface at that
// x,z (within +/-3 blocks of the previous point) and must have line of sight from the previous point.
// If a point can't find ground in range or is walled off, the chain stops there. The resolved points
// then erupt one per second, dealing blunt damage and Concussed in a radius around each.
public class Stonefang extends Equipment {
	private static final String ID = "Stonefang";
	private static final int[] DISTANCES = { 3, 6, 9 };
	private static final int LEEWAY = 3; // max blocks up/down the surface may shift between points
	private static final double AOE = 2;
	private static final TargetProperties tp = TargetProperties.radius(AOE, false, TargetType.ENEMY);

	// --- FX (see @fx) ---
	// AoE indicator (matches the radius-2 strike) and a small ring around the caster's feet for the windup.
	private static final Circle circ = new Circle(AOE);
	private static final Circle feetCircle = new Circle(1.4);

	// Charge windup: a crisp stone ring closing in on the feet + dirt bits rising to telegraph the channel.
	private static final ParticleContainer chargeEdge = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.STONE.createBlockData()).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer chargeRise = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.DIRT.createBlockData()).count(3).spread(0.15, 0.1).speed(0.01).offsetY(0.2);

	// Burrowing fang: a thin, ground-hugging dirt trail with a spark tip tunnelling toward each strike point.
	private static final ParticleContainer trail = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.DIRT.createBlockData()).count(2).spread(0.12, 0.05).speed(0.01);
	private static final ParticleContainer trailSpark = new ParticleContainer(Particle.CRIT)
			.count(1).spread(0.1, 0.05).speed(0.01);

	// Eruption: crisp AoE boundary (deepslate) + dirt fill for clarity, plus a stone plume and crit debris burst.
	private static final ParticleContainer eruptEdge = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.DEEPSLATE.createBlockData()).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer eruptFill = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.DIRT.createBlockData()).count(1).spread(0.1, 0).speed(0);
	private static final ParticleContainer eruptBurst = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.STONE.createBlockData()).count(12).spread(0.3, 0.2).speed(0.08).offsetY(0.4);
	private static final ParticleContainer eruptSpark = new ParticleContainer(Particle.CRIT)
			.count(16).spread(0.4, 0.5).speed(0.06).offsetY(0.3);

	private static final SoundContainer eruptSound = new SoundContainer(Sound.BLOCK_STONE_BREAK);
	private static final SoundContainer castSound = new SoundContainer(Sound.BLOCK_GRAVEL_BREAK);
	private static final SoundContainer burrowSound = new SoundContainer(Sound.BLOCK_ROOTED_DIRT_BREAK, 0.9F);
	private static final SoundContainer chargeSound = new SoundContainer(Sound.BLOCK_GRAVEL_HIT, 0.8F);

	private int damage, conc;

	public Stonefang(boolean isUpgraded) {
		super(ID, "Stonefang", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(25, 10, 10, 9));
		damage = isUpgraded ? 150 : 100;
		conc = isUpgraded ? 8 : 5;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		Equipment eq = this;
		data.addTrigger(id, bind, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			castSound.play(p, p);
			// Windup telegraph: a stone ring and rising dirt gather at the caster's feet during the 1s channel.
			data.addTask(new BukkitRunnable() {
				int ticks = 0;

				@Override
				public void run() {
					if (ticks >= 20) {
						cancel();
						return;
					}
					Player pl = data.getPlayer();
					Location feet = pl.getLocation();
					feetCircle.play(chargeEdge, feet, LocalAxes.xz(), null);
					chargeRise.play(pl, feet);
					if (ticks % 8 == 0) chargeSound.play(pl, feet);
					ticks += 4;
				}
			}.runTaskTimer(NeoRogue.inst(), 0L, 4L));
			data.charge(20).then(() -> fire(data, eq, slot));
			return TriggerResult.keep();
		}));
	}

	// Resolves the strike chain and schedules the staggered eruptions.
	private void fire(PlayerFightData data, Equipment eq, int slot) {
		Player p = data.getPlayer();
		World world = p.getWorld();
		Vector dir = p.getLocation().getDirection().setY(0);
		if (dir.lengthSquared() < 1.0e-6) return; // looking straight up/down; no horizontal facing
		dir.normalize();

		Location origin = p.getLocation();
		ArrayList<Location> points = new ArrayList<Location>();
		// The wall check for the first segment originates from the player; afterwards from the prior point.
		Location prevRayPoint = origin.clone().add(0, 1, 0);
		int prevGroundY = origin.getBlockY() - 1; // solid block the player is standing on

		for (int d : DISTANCES) {
			int bx = (int) Math.floor(origin.getX() + dir.getX() * d);
			int bz = (int) Math.floor(origin.getZ() + dir.getZ() * d);
			Integer groundY = findGround(world, bx, bz, prevGroundY);
			if (groundY == null) break; // cliff/gap beyond leeway: stop the chain

			Location surface = new Location(world, bx + 0.5, groundY + 1, bz + 0.5);
			Location rayTarget = surface.clone().add(0, 0.5, 0);
			if (TargetHelper.isObstructed(prevRayPoint, rayTarget)) break; // walled off: stop the chain

			points.add(surface);
			prevRayPoint = rayTarget;
			prevGroundY = groundY;
		}

		for (int i = 0; i < points.size(); i++) {
			Location point = points.get(i);
			// The fang burrows from the caster's feet to the first point, then from each point to the next.
			Location from = (i == 0) ? origin.clone() : points.get(i - 1).clone();
			if (i == 0) {
				burrow(data, from, point);
				erupt(data, eq, slot, point);
			} else {
				data.addTask(new BukkitRunnable() {
					@Override
					public void run() {
						burrow(data, from, point);
						erupt(data, eq, slot, point);
					}
				}.runTaskLater(NeoRogue.inst(), i * 10L));
			}
		}
	}

	// Finds the ground surface Y at (bx, bz): the highest solid block with a passable block above,
	// searched within +/-LEEWAY of the previous point's ground. Returns null if none in range.
	private static Integer findGround(World world, int bx, int bz, int prevGroundY) {
		for (int y = prevGroundY + LEEWAY; y >= prevGroundY - LEEWAY; y--) {
			Block b = world.getBlockAt(bx, y, bz);
			if (!b.getType().isSolid()) continue;
			if (!world.getBlockAt(bx, y + 1, bz).isPassable()) continue;
			return y;
		}
		return null;
	}

	// Draws the ground-hugging "fang" trail from the previous point to a strike point right as it lands.
	private void burrow(PlayerFightData data, Location from, Location to) {
		Player p = data.getPlayer();
		Location a = from.clone().add(0, 0.2, 0);
		Location b = to.clone().add(0, 0.2, 0);
		ParticleUtil.drawLine(p, trail, a, b, 0.25);
		ParticleUtil.drawLine(p, trailSpark, a, b, 0.5);
		burrowSound.play(p, to);
	}

	// Deals damage + Concussed to enemies in the AoE around a resolved strike point.
	private void erupt(PlayerFightData data, Equipment eq, int slot, Location surface) {
		Player p = data.getPlayer();
		Location center = surface.clone().add(0, 0.5, 0);
		circ.play(eruptEdge, surface, LocalAxes.xz(), eruptFill);
		eruptBurst.play(p, center);
		eruptSpark.play(p, center);
		eruptSound.play(p, surface);
		Sounds.explode.play(p, surface);
		Sounds.anvil.play(p, surface);
		for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, center, tp)) {
			FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.BLUNT, DamageStatTracker.of(id + slot, eq)), ent);
			FightInstance.applyStatus(ent, StatusType.CONCUSSED, data, conc, -1, Stonefang.this);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POINTED_DRIPSTONE,
				"On cast, " + GlossaryTag.CHANNEL.tag(this) + " for " + DescUtil.val("1s") + ", then send a fang of stone "
						+ "burrowing along the ground " + DescUtil.val("3") + ", " + DescUtil.val("6") + ", and "
						+ DescUtil.val("9") + " blocks ahead along the ground in rapid succession. Each strike deals "
						+ GlossaryTag.BLUNT.tag(this, damage) + " damage and applies "
						+ GlossaryTag.CONCUSSED.tag(this, conc) + ".");
	}
}
