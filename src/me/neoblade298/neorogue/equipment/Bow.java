package me.neoblade298.neorogue.equipment;

import java.util.HashMap;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffSlice;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerCondition;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public abstract class Bow extends Equipment {
	public static TriggerCondition needsAmmo = (p, data) -> {
		if (data.getAmmoInstance() == null) {
			Util.displayError(data.getPlayer(), "You don't have any ammunition equipped!");
			return false;
		}
		return true;
	};

	// Vector is non-normalized velocity of the vanilla projectile being fired
	public Bow(String id, String display, boolean isUpgraded, Rarity rarity, EquipmentClass ec, EquipmentType type, EquipmentProperties props) {
		super(id, display, isUpgraded, rarity, ec, type, props);
	}

	public abstract void onTick(Player p, ProjectileInstance proj, int interpolation);

	public boolean canShoot(PlayerFightData data) {
		if (data.getAmmoInstance() == null) { 
			Util.displayError(data.getPlayer(), "You don't have any ammunition equipped!");
			return false;
		}
		return true;
	}

	public void bowDamageProjectile(LivingEntity target, ProjectileInstance proj, Barrier hitBarrier, AmmunitionInstance ammo, double initialVelocity, boolean basicAttack) {
		DamageMeta dm = proj.getMeta();
		PlayerFightData data = (PlayerFightData) proj.getOwner();

		// Apply any ammo changes
		ammo.onHit(proj, target);

		// Multiply damage by the initial velocity of the projectile
		dm.addBuff(BuffType.GENERAL, new Buff(0, (initialVelocity / 3) - 1, new HashMap<FightData, BuffSlice>()), BuffOrigin.INITIAL_VELOCITY, true);
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
					proj.getVelocity().normalize().multiply(properties.get(PropertyType.KNOCKBACK) + ammo.getProperties().get(PropertyType.KNOCKBACK)));
		}
		FightInstance.dealDamage(dm, target);
	}
}
