package me.neoblade298.neorogue.equipment;

import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.TriggerCondition;

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
}
