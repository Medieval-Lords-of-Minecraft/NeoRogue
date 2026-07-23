package me.neoblade298.neorogue.equipment.abilities;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleAnimation;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

// Uncommon mage active. On cast, summons a shard that orbits the player, dealing piercing damage on
// contact (shared per-enemy cooldown). Striking a Concussed enemy summons another shard, up to a cap.
public class ShardCloak extends Equipment {
	private static final String ID = "ShardCloak";
	private static final double ORBIT_RADIUS = 4;
	private static final double HIT_RADIUS = 1.5;
	private static final double ORBIT_PERIOD_TICKS = 40; // configurable orbit speed: ticks per full revolution
	private static final int DURATION_TICKS = 80; // each orbital lasts 4s
	private static final int MAX_ORBITALS = 4;
	private static final long HIT_COOLDOWN_MS = 1000; // per-enemy cooldown, shared across all orbitals
	private static final TargetProperties hitTp = TargetProperties.radius(HIT_RADIUS, false, TargetType.ENEMY);
	private static final Color AMETHYST = Color.fromRGB(149, 97, 214);

	// Orbital shard: crisp amethyst crystal body with a glowing purple core and a bright shimmering trail.
	private static final ParticleContainer orbPc = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.AMETHYST_BLOCK.createBlockData()).count(2).spread(0.1, 0.1).speed(0.01);
	private static final ParticleContainer orbCorePc = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(AMETHYST, 1.2F)).count(1).spread(0.05, 0.05).speed(0);
	private static final ParticleContainer trailPc = new ParticleContainer(Particle.END_ROD)
			.count(1).spread(0.05, 0.05).speed(0.01);

	// Cast flourish: a quick amethyst burst at the caster plus an expanding ring that telegraphs the orbit path.
	private static final ParticleContainer castBurstPc = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.AMETHYST_BLOCK.createBlockData()).count(12).spread(0.3, 0.5).speed(0.03);
	private static final ParticleContainer castRingPc = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(AMETHYST, 1.0F)).count(1).spread(0, 0).speed(0);
	private static final ParticleAnimation castRingAnim;

	// Impact burst: shattering amethyst shards with a spray of bright sparkles (intentional impact = larger spread/speed).
	private static final ParticleContainer hitPc = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.AMETHYST_BLOCK.createBlockData()).count(15).spread(0.4, 0.4).speed(0.05);
	private static final ParticleContainer hitSparklePc = new ParticleContainer(Particle.END_ROD)
			.count(8).spread(0.3, 0.3).speed(0.05);

	// Fracture flourish: an extra shard splits off a Concussed enemy.
	private static final ParticleContainer fracturePc = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.AMETHYST_BLOCK.createBlockData()).count(20).spread(0.3, 0.3).speed(0.08);
	private static final ParticleContainer fractureDustPc = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(AMETHYST, 1.3F)).count(10).spread(0.3, 0.3).speed(0.05);

	private static final SoundContainer castSound = new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_CHIME);
	private static final SoundContainer hitSound = new SoundContainer(Sound.BLOCK_SHELF_ACTIVATE);
	private static final SoundContainer fractureSound = new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_RESONATE);

	static {
		// Expanding amethyst ring drawn at chest height, sweeping out to the orbit radius over 8 ticks.
		castRingAnim = new ParticleAnimation(castRingPc, (loc, tick) -> {
			LinkedList<Location> locs = new LinkedList<Location>();
			double radius = ORBIT_RADIUS * (tick + 1) / 8.0;
			int points = 16 + tick * 2;
			for (int i = 0; i < points; i++) {
				double angle = 2 * Math.PI * i / points + tick * 0.25;
				locs.add(loc.clone().add(Math.cos(angle) * radius, 1.0, Math.sin(angle) * radius));
			}
			return locs;
		}, 8);
	}

	private int damage;

	public ShardCloak(boolean isUpgraded) {
		super(ID, "Shard Cloak", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(20, 10, 15, ORBIT_RADIUS));
		damage = isUpgraded ? 90 : 60;
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
			Location castLoc = p.getLocation();
			castBurstPc.play(p, castLoc.clone().add(0, 1.0, 0));
			data.runAnimation(id, p, castRingAnim, castLoc);
			CloakState state = new CloakState();
			spawnOrbital(data, eq, slot, state, 0);
			return TriggerResult.keep();
		}));
	}

	// Shared state across every orbital spawned from a single cast: the live orbital count (capped at
	// MAX_ORBITALS) and the per-enemy hit cooldown so multiple orbitals can't gang up on one target.
	private static class CloakState {
		int orbitals = 0;
		final HashMap<UUID, Long> hitCd = new HashMap<UUID, Long>();
	}

	private void spawnOrbital(PlayerFightData data, Equipment eq, int slot, CloakState state, double startAngle) {
		if (state.orbitals >= MAX_ORBITALS) return;
		state.orbitals++;
		data.addTask(new BukkitRunnable() {
			private int tick = 0;

			@Override
			public void run() {
				Player p = data.getPlayer();
				if (tick >= DURATION_TICKS || p == null || !p.isValid()) {
					state.orbitals--;
					cancel();
					return;
				}

				double angle = startAngle + tick * (2 * Math.PI / ORBIT_PERIOD_TICKS);
				Location orbLoc = p.getLocation().add(Math.cos(angle) * ORBIT_RADIUS, 1.0, Math.sin(angle) * ORBIT_RADIUS);
				orbPc.play(p, orbLoc);
				orbCorePc.play(p, orbLoc);
				trailPc.play(p, orbLoc);

				LivingEntity target = TargetHelper.getNearest(p, orbLoc, hitTp);
				if (target != null) {
					long now = System.currentTimeMillis();
					Long last = state.hitCd.get(target.getUniqueId());
					if (last == null || now - last >= HIT_COOLDOWN_MS) {
						state.hitCd.put(target.getUniqueId(), now);
						FightInstance.dealDamage(data, DamageType.PIERCING, damage, target,
								DamageStatTracker.of(id + slot, eq));
						Location hitLoc = target.getLocation().add(0, 1, 0);
						hitPc.play(p, hitLoc);
						hitSparklePc.play(p, hitLoc);
						hitSound.play(p, target.getLocation());

						// Striking a Concussed enemy fractures off another shard (up to the cap).
						FightData tfd = FightInstance.getFightData(target.getUniqueId());
						if (tfd != null && tfd.hasStatus(StatusType.CONCUSSED)) {
							fracturePc.play(p, hitLoc);
							fractureDustPc.play(p, hitLoc);
							fractureSound.play(p, target.getLocation());
							spawnOrbital(data, eq, slot, state, angle + Math.PI);
						}
					}
				}

				tick++;
			}
		}.runTaskTimer(NeoRogue.inst(), 0L, 1L));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.AMETHYST_SHARD,
				"On cast, summon a shard that orbits you at " + DescUtil.val((int) ORBIT_RADIUS) + " blocks for "
						+ DescUtil.duration(DURATION_TICKS / 20) + ". On contact it deals "
						+ GlossaryTag.PIERCING.tag(this, damage) + " damage to an enemy ("
						+ DescUtil.val("1s") + " cooldown per enemy). Striking a "
						+ GlossaryTag.CONCUSSED.tag(this) + " enemy summons another shard, up to "
						+ DescUtil.val(MAX_ORBITALS) + " at once.");
	}
}
