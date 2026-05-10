package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.AmmunitionInstance;
import me.neoblade298.neorogue.equipment.BowProjectile;
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
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class DangerousGame extends Equipment {
	private static final String ID = "DangerousGame";
	private int range, damage;
	
	public DangerousGame(boolean isUpgraded) {
		super(ID, "Dangerous Game", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 0, 0, 0));
		range = 5;
		damage = isUpgraded ? 70 : 50;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(data.getPlayer(), data.getPlayer());

			data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata2, in2) -> {
				BasicAttackEvent ev = (BasicAttackEvent) in2;
				ProjectileGroup group = new ProjectileGroup(new DangerousGameProjectile(data, this, slot));
				
				LivingEntity target = ev.getTarget();
				if (target == null) return TriggerResult.keep();
				
				// Check if target is within 5 blocks
				if (ev.getProjectile().getOrigin().distance(target.getLocation()) <= range && data.hasAmmoInstance()) {
					data.addExtraShot(group);
				}
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.TARGET,
				GlossaryTag.POWER.tag(this) + ". When you land a basic attack on an enemy within " + DescUtil.white(range) + " blocks, " +
				"your next basic attack will fire an additional projectile at them using your current ammunition that deals an additional " + DescUtil.yellow(damage) + " damage.");
	}

	private class DangerousGameProjectile extends Projectile {
		private AmmunitionInstance ammo;
		private PlayerFightData data;
		private Equipment eq;
		private int slot;
		
		public DangerousGameProjectile(PlayerFightData data, Equipment eq, int slot) {
			super(12, 1);
			this.blocksPerTick(3);
			this.homing(0.02);
			this.data = data;
			this.ammo = data.getAmmoInstance();
			this.eq = eq;
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			BowProjectile.tick.play(data.getPlayer(), proj.getLocation());
			ammo.onTick(data.getPlayer(), proj, interpolation);
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			ammo.onHit(proj, meta, hit.getEntity());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			DamageMeta dm = proj.getMeta();
			EquipmentProperties ammoProps = ammo.getProperties();
			double dmg = ammoProps.get(PropertyType.DAMAGE);
			dm.addDamageSlice(new DamageSlice(data, damage, ammoProps.getType(), DamageStatTracker.of(id + slot, eq)));
			dm.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), Buff.increase(data, dmg, BuffStatTracker.arrowBuff(ammo.getAmmo())));
			Sounds.shoot.play(data.getPlayer(), data.getPlayer());
			ammo.onStart(proj);
		}
	}
}
