package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
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

public class BrightestFlame extends Equipment {
	private static final String ID = "BrightestFlame";
	private static final TargetProperties tp = TargetProperties.radius(20, false, TargetType.ENEMY);
	private static final ParticleContainer trail = new ParticleContainer(Particle.FLAME).count(5).spread(0.2, 0.2);

	private int damage, burn, corr;

	public BrightestFlame(boolean isUpgraded) {
		super(ID, "Brightest Flame", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(isUpgraded ? 60 : 80, 0, 0, 0));
		damage = isUpgraded ? 120 : 80;
		burn = isUpgraded ? 6 : 4;
		corr = 2;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup();
		// Create 5 projectiles in upward fan: -45, -22.5, 0, 22.5, 45 degrees
		for (double angle : new double[] { -45, -22.5, 0, 22.5, 45 }) {
			proj.add(new BrightestFlameProjectile(data, angle, this, slot));
		}
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			Sounds.equip.play(p, p);
			data.applyStatus(StatusType.CORRUPTION, data, corr, -1);
			proj.start(data);
			return TriggerResult.keep();
		}));
	}

	private class BrightestFlameProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;
		private int slot;
		private Equipment eq;

		public BrightestFlameProjectile(PlayerFightData data, double angleOffset, Equipment eq, int slot) {
			super(0.8, properties.get(PropertyType.RANGE), 1);
			this.size(0.4, 0.4);
			this.rotation(angleOffset);
			this.homing(0.02);
			this.data = data;
			this.p = data.getPlayer();
			this.eq = eq;
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			trail.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			LivingEntity target = hit.getEntity();
			
			// Deal fire damage
			meta.addDamageSlice(new DamageSlice(data, damage, DamageType.FIRE, 
					DamageStatTracker.of(ID + slot, eq)));
			
			// Apply burn
			FightInstance.applyStatus(target, StatusType.BURN, data, burn, -1);
			
			Sounds.fire.play(p, target.getLocation());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			// Set homing target to nearest enemy
			LivingEntity nearest = TargetHelper.getNearest(p, proj.getLocation(), tp);
			if (nearest != null) {
				proj.setHomingTarget(nearest);
			}
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FIRE_CHARGE,
				"Fire " + DescUtil.yellow("5") + " homing projectiles in an upward fan that each deal " +
				GlossaryTag.FIRE.tag(this, damage, true) + " damage and apply " +
				GlossaryTag.BURN.tag(this, burn, true) + ". Gain " +
				GlossaryTag.CORRUPTION.tag(this, corr, false) + ".");
	}
}
