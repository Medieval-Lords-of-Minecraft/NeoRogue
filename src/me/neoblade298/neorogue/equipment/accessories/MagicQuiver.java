package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.AmmunitionInstance;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreLaunchProjectileGroupEvent;

public class MagicQuiver extends Equipment {
	private static final String ID = "MagicQuiver";
	private static final TargetProperties tp = TargetProperties.radius(12, false, TargetType.ENEMY);
	private int thres, damage;
	
	public MagicQuiver(boolean isUpgraded) {
		super(ID, "Magic Quiver", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ACCESSORY);
		thres = isUpgraded ? 2 : 3;
		damage = 20;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardPriorityAction action = new StandardPriorityAction(id);

		action.setAction((pdata, in) -> {
			PreLaunchProjectileGroupEvent ev = (PreLaunchProjectileGroupEvent) in;
			if (!ev.isBasicAttack()) return TriggerResult.keep();
			action.addCount(1);
			Player p = data.getPlayer();
			LivingEntity trg = TargetHelper.getNearest(p, tp);
			ProjectileGroup group = new ProjectileGroup(new MagicQuiverProjectile(data, this, slot));
			if (action.getCount() >= thres && data.hasAmmoInstance() && trg != null) {
				action.addCount(-thres);
				data.addExtraShot(group);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.PRE_LAUNCH_PROJECTILE_GROUP, action);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT, "Every " + DescUtil.yellow(thres == 3 ? "3rd" : "2nd") + " time you launch a basic attack, also fire a " +
				"homing projectile towards the nearest enemy using your current ammunition that deals " + DescUtil.white(damage) + " damage.");
	}

	private class MagicQuiverProjectile extends Projectile {
		private AmmunitionInstance ammo;
		private PlayerFightData data;
		private Player p;
		private Equipment eq;
		private int slot;
		public MagicQuiverProjectile(PlayerFightData data, Equipment eq, int slot) {
			super(tp.range, 1);
			this.blocksPerTick(3);
			this.homing(0.02);
			this.data = data;
			this.p = data.getPlayer();
			ammo = data.getAmmoInstance();
			this.eq = eq;
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			BowProjectile.tick.play(p, proj.getLocation());
			ammo.onTick(p, proj, interpolation);
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			ammo.onHit(proj, meta, hit.getEntity());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.shoot.play(p, p);
			DamageMeta dm = proj.getMeta();
			EquipmentProperties ammoProps = ammo.getProperties();
			double dmg = ammoProps.get(PropertyType.DAMAGE);
			dm.addDamageSlice(new DamageSlice(data, damage, ammoProps.getType(), DamageStatTracker.of(id + slot, eq)));
			dm.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), Buff.increase(data, dmg, BuffStatTracker.arrowBuff(ammo.getAmmo())));
			ammo.onStart(proj);
		}
	}
}
