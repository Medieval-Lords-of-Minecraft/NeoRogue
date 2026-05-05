package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Overgrowth extends Equipment {
	private static final String ID = "Overgrowth";
	private static final int RADIUS = 5, RANGE = 12;
	private static final TargetProperties tp = TargetProperties.radius(RADIUS, true);
	private static final Circle circ = new Circle(RADIUS);
	private static final ParticleContainer projPart = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(50, 180, 50), 1.2F))
			.count(3).spread(0.1, 0.1).speed(0);
	private static final ParticleContainer edge = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(30, 130, 30), 1F))
			.count(1).spread(0, 0).speed(0);
	private static final ParticleContainer fill = new ParticleContainer(Particle.SPORE_BLOSSOM_AIR)
			.count(1).spread(0.1, 0).speed(0);
	private static final ParticleContainer impact = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.GRASS_BLOCK.createBlockData())
			.count(50).spread(2.5, 0.5).speed(0.2);

	private int damage;

	public Overgrowth(boolean isUpgraded) {
		super(ID, "Overgrowth", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 0, 12, RANGE, RADIUS));
		damage = isUpgraded ? 225 : 150;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup group = new ProjectileGroup(new OvergrowthProjectile(data, slot));
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			group.start(data);
			return TriggerResult.keep();
		}));
	}

	private void explodeArea(Player p, PlayerFightData data, Location loc, int slot) {
		// Snap to ground
		while (loc.getBlock().getType().isAir() && loc.getY() > 0) {
			loc.add(0, -1, 0);
		}
		loc.add(0, 1, 0);

		impact.play(p, loc);
		circ.play(edge, loc, LocalAxes.xz(), fill);
		Sounds.explode.play(p, loc);

		for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, tp)) {
			FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.EARTHEN,
					DamageStatTracker.of(id + slot, this)), ent);
		}

		FightInstance.terraformGrass(loc, RADIUS);
	}

	private class OvergrowthProjectile extends Projectile {
		private final PlayerFightData data;
		private final int slot;

		public OvergrowthProjectile(PlayerFightData data, int slot) {
			super(1, RANGE, 2);
			this.gravity(0.05);
			this.arc(0.5);
			this.size(0.5, 0.5);
			this.data = data;
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			projPart.play(data.getPlayer(), proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			explodeArea(data.getPlayer(), data, hit.getEntity().getLocation(), slot);
		}

		@Override
		public void onHitBlock(ProjectileInstance proj, Block b) {
			explodeArea(data.getPlayer(), data, b.getLocation(), slot);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.threw.play(data.getPlayer(), proj.getLocation());
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.MOSS_BLOCK,
				"Lob a potion that explodes on impact, dealing " + GlossaryTag.EARTHEN.tag(this, damage, true)
				+ " damage to nearby enemies and turning nearby terrain to grass.");
	}
}
