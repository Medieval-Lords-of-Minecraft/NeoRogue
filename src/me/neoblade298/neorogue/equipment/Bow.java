package me.neoblade298.neorogue.equipment;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerCondition;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public abstract class Bow extends Equipment {
	public static TriggerCondition needsAmmo = (p, data, in) -> {
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
		if (data.getMana() < properties.get(PropertyType.MANA_COST)) {
			Util.displayError(data.getPlayer(), "Not enough mana!");
			return false;
		}

		if (data.getStamina() < properties.get(PropertyType.STAMINA_COST)) {
			Util.displayError(data.getPlayer(), "Not enough stamina!");
			return false;
		}

		if (!data.canBasicAttack(EquipSlot.HOTBAR)) {
			return false;
		}
		return true;
	}

	// Crossbow attack speed is built into the draw
	public boolean canShootCrossbow(PlayerFightData data) {
		if (data.getAmmoInstance() == null) { 
			Util.displayError(data.getPlayer(), "You don't have any ammunition equipped!");
			return false;
		}
		if (data.getMana() < properties.get(PropertyType.MANA_COST)) {
			Util.displayError(data.getPlayer(), "Not enough mana!");
			return false;
		}

		if (data.getStamina() < properties.get(PropertyType.STAMINA_COST)) {
			Util.displayError(data.getPlayer(), "Not enough stamina!");
			return false;
		}
		return true;
	}

	public void useBow(PlayerFightData data) {
		if (properties.has(PropertyType.MANA_COST)) data.addMana(-properties.get(PropertyType.MANA_COST));
		if (properties.has(PropertyType.STAMINA_COST)) data.addStamina(-properties.get(PropertyType.STAMINA_COST));
		data.setBasicAttackCooldown(EquipSlot.HOTBAR, properties.get(PropertyType.ATTACK_SPEED));
	}

	public void bowProjectileOnHit(LivingEntity target, ProjectileInstance proj, Barrier hitBarrier, DamageMeta dm, AmmunitionInstance ammo, boolean basicAttack) {
		PlayerFightData data = (PlayerFightData) proj.getOwner();

		if (basicAttack) {
			dm.setBasicAttack(true);
			PreBasicAttackEvent ev = new PreBasicAttackEvent(target, dm, properties.get(PropertyType.KNOCKBACK), this, proj);
			data.runActions(data, Trigger.PRE_BASIC_ATTACK, ev);
		}
		if (properties.contains(PropertyType.KNOCKBACK)) {
			FightInstance.knockback(target,
					proj.getVelocity().normalize().multiply(properties.get(PropertyType.KNOCKBACK) + ammo.getProperties().get(PropertyType.KNOCKBACK)));
		}
		if (basicAttack) {
			BasicAttackEvent ev = new BasicAttackEvent(target, dm, properties.get(PropertyType.KNOCKBACK), this, proj);
			data.runActions(data, Trigger.BASIC_ATTACK, ev);
		}
	}
}
