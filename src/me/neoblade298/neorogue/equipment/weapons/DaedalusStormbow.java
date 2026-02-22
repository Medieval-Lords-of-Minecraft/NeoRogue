package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.AmmunitionInstance;
import me.neoblade298.neorogue.equipment.Bow;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.IProjectileInstance;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class DaedalusStormbow extends Bow {
	private static final String ID = "DaedalusStormbow";
	private static final int EXTRA_SHOT_DAMAGE_INCREMENT = 5;
	private int threshold, damage;
	
	public DaedalusStormbow(boolean isUpgraded) {
		super(ID, "Daedalus Stormbow", isUpgraded, Rarity.EPIC, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofBow(55, 1, 0, 12, 0, 0));
		threshold = isUpgraded ? 7 : 10;
		damage = 40;
	}

	@Override
	public void onTick(Player p, ProjectileInstance proj, int interpolation) {
		BowProjectile.tick.play(p, proj.getLocation());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ItemStack icon = item.clone();
		ItemStack chargedIcon = icon.clone();
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		
		// Track projectile damage hits and extra shot damage
		ActionMeta am = new ActionMeta();
		
		// Standard bow shooting with extra shot
		data.addSlotBasedTrigger(id, slot, Trigger.VANILLA_PROJECTILE, (pdata, in) -> {
			if (!canShoot(data)) return TriggerResult.keep();
			useBow(data);

			ProjectileLaunchEvent ev = (ProjectileLaunchEvent) in;
			Vector velocity = ev.getEntity().getVelocity();
			
			ProjectileGroup proj = new ProjectileGroup(new BowProjectile(data, velocity, this, id + slot));
			
			// Add extra shot with rotation
			DaedalusStormbowProjectile extraShot = new DaedalusStormbowProjectile(data, this, slot);
			extraShot.rotation(15);
			ProjectileGroup extraShotGroup = new ProjectileGroup(extraShot);
			data.addExtraShot(extraShotGroup);
			
			proj.start(data);
			return TriggerResult.keep();
		});
		
		// Increase basic attack projectile range by 4
		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata, in) -> {
			LaunchProjectileGroupEvent ev = (LaunchProjectileGroupEvent) in;
			if (!ev.isBasicAttack()) return TriggerResult.keep();
			if (!(ev.getInstances().getFirst() instanceof ProjectileInstance)) return TriggerResult.keep();
			for (IProjectileInstance ipi : ev.getInstances()) {
				((ProjectileInstance) ipi).addMaxRange(4);
			}
			return TriggerResult.keep();
		});
		
		// Buff all extra shot damage
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			if (!ev.getMeta().hasTag(PlayerFightData.EXTRA_SHOT_TAG)) return TriggerResult.keep();
			double extraShotDamage = 40 + am.getInt();
			ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
					Buff.increase(data, extraShotDamage, BuffStatTracker.damageBuffAlly(am.getId(), this)));
			return TriggerResult.keep();
		});
		
		// Track projectile damage to increase extra shot damage
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (!ev.getMeta().hasOrigin(DamageOrigin.PROJECTILE)) return TriggerResult.keep();
			
			am.addCount(1);
			
			if (am.getCount() >= threshold) {
				am.addCount(-threshold);
				am.addInt(EXTRA_SHOT_DAMAGE_INCREMENT);
				
				// Update icon to show extra shot damage
				ItemStack currentIcon = chargedIcon.clone();
				currentIcon.setAmount(Math.min(64, am.getInt() / EXTRA_SHOT_DAMAGE_INCREMENT));
				inst.setIcon(currentIcon);
				
				Player p = data.getPlayer();
				Sounds.fire.play(p, p);
			}
			
			return TriggerResult.keep();
		});
	}

	private class DaedalusStormbowProjectile extends Projectile {
		private AmmunitionInstance ammo;
		private PlayerFightData data;
		private Player p;
		private Equipment eq;
		private int slot;
		public DaedalusStormbowProjectile(PlayerFightData data, Equipment eq, int slot) {
			super(properties.get(PropertyType.RANGE), 1);
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

	@Override
	public void setupItem() {
		item = createItem(Material.BOW,
				"Passive. Increase basic attack range by <white>4</white> and fire an extra shot on basic attack launch. " +
				"Every " + DescUtil.yellow(threshold) + " times you deal projectile damage, increases the damage your extra shots " +
				"deal by " + DescUtil.yellow(EXTRA_SHOT_DAMAGE_INCREMENT) + ".");
	}
}
