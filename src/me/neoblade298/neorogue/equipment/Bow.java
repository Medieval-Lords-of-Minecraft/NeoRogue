package me.neoblade298.neorogue.equipment;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public abstract class Bow extends Equipment {

	// Vector is non-normalized velocity of the vanilla projectile being fired
	public Bow(String id, String display, boolean isUpgraded, Rarity rarity, EquipmentClass ec, EquipmentType type, EquipmentProperties props) {
		super(id, display, isUpgraded, rarity, ec, type, props);
	}

	public abstract void onTick(Player p, ProjectileInstance proj, boolean interpolation);

	public boolean canShoot(PlayerFightData data) {
		if (data.getAmmunition() == null) { 
			Util.displayError(data.getPlayer(), "You don't have any ammunition equipped!");
			return false;
		}
		return true;
	}

	public void bowDamageProjectile(LivingEntity target, ProjectileInstance proj, Barrier hitBarrier, Ammunition ammo, double initialVelocity, boolean basicAttack) {
		DamageMeta dm = new DamageMeta((PlayerFightData) proj.getOwner(),
			(properties.get(PropertyType.DAMAGE) + ammo.getProperties().get(PropertyType.DAMAGE)) * (initialVelocity / 3), ammo.getProperties().getType());
		PlayerFightData data = (PlayerFightData) proj.getOwner();
		if (!proj.getBuffs().isEmpty()) {
			dm.addBuffs(proj.getBuffs(), BuffOrigin.PROJECTILE, true);
		}
		if (hitBarrier != null) {
			dm.addBuffs(hitBarrier.getBuffs(), BuffOrigin.BARRIER, false);
		}
		if (basicAttack) {
			BasicAttackEvent ev = new BasicAttackEvent(target, dm, properties.get(PropertyType.KNOCKBACK), this, null);
			data.runActions(data, Trigger.BASIC_ATTACK, ev);
		}
		if (properties.contains(PropertyType.KNOCKBACK)) {
			FightInstance.knockback(target,
					proj.getVector().normalize().multiply(properties.get(PropertyType.KNOCKBACK) + ammo.getProperties().get(PropertyType.KNOCKBACK)));
		}
		FightInstance.dealDamage(dm, target);
	}

	public void bowDamageProjectile(LivingEntity target, ProjectileInstance proj, DamageMeta dm, Barrier hitBarrier, Ammunition ammo, boolean basicAttack) {
		PlayerFightData data = (PlayerFightData) proj.getOwner();
		if (!proj.getBuffs().isEmpty()) {
			dm.addBuffs(proj.getBuffs(), BuffOrigin.PROJECTILE, true);
		}
		if (hitBarrier != null) {
			dm.addBuffs(hitBarrier.getBuffs(), BuffOrigin.BARRIER, false);
		}
		if (basicAttack) {
			BasicAttackEvent ev = new BasicAttackEvent(target, dm, properties.get(PropertyType.KNOCKBACK), this, null);
			data.runActions(data, Trigger.BASIC_ATTACK, ev);
		}
		if (properties.contains(PropertyType.KNOCKBACK)) {
			FightInstance.knockback(target,
					proj.getVector().normalize().multiply(properties.get(PropertyType.KNOCKBACK) + ammo.getProperties().get(PropertyType.KNOCKBACK)));
		}
		FightInstance.dealDamage(dm, target);
	}
}
