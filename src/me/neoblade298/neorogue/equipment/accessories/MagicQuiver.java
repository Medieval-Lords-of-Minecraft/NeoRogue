package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
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
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class MagicQuiver extends Equipment {
	private static final String ID = "magicQuiver";
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
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardPriorityAction action = new StandardPriorityAction(id);

		action.setAction((pdata, in) -> {
			ProjectileGroup group = new ProjectileGroup(new MagicQuiverProjectile(data));
			LaunchProjectileGroupEvent ev = (LaunchProjectileGroupEvent) in;
			if (!ev.isBasicAttack()) return TriggerResult.keep();
			action.addCount(1);
			LivingEntity trg = TargetHelper.getNearest(p, tp);
			if (action.getCount() >= thres && data.hasAmmoInstance() && trg != null) {
				data.addTask(new BukkitRunnable() {
					public void run() {
						group.start(data);
					}
				}.runTaskLater(NeoRogue.inst(), 5));
				action.addCount(-thres);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, action);
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
		public MagicQuiverProjectile(PlayerFightData data) {
			super(tp.range, 1);
			this.blocksPerTick(3);
			this.homing(0.2);
			this.data = data;
			this.p = data.getPlayer();
			ammo = data.getAmmoInstance();
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
			dm.addDamageSlice(new DamageSlice(data, damage + dmg, ammoProps.getType()));
			ammo.onStart(proj);
		}
	}
}
