package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public abstract class HotbarCompatible extends Equipment {
	protected double manaCost, staminaCost;
	public HotbarCompatible(String id, boolean isUpgraded, Rarity rarity, EquipmentClass ec) {
		super(id, isUpgraded, rarity, ec);
	}
	
	protected void addToLore(ArrayList<String> lore) {
		if (manaCost > 0) lore.add("§6Mana Cost: §e" + manaCost);
		if (staminaCost > 0) lore.add("§6Stamina Cost: §e" + staminaCost);
	}
	
	protected boolean canCast(PlayerFightData data) {
		if (data.getMana() <= manaCost) {
			Util.displayError(data.getPlayer(), "&cNot enough mana!");
			return false;
		}
		
		if (data.getStamina() <= staminaCost) {
			Util.displayError(data.getPlayer(), "&cNot enough stamina!");
			return false;
		}
		
		data.addMana(-manaCost);
		data.addStamina(-staminaCost);
		return true;
	}
}
