package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;

public abstract class HotbarCompatible extends Equipment {
	protected double manaCost, staminaCost;
	public HotbarCompatible(String id, String display, boolean isUpgraded, Rarity rarity, EquipmentClass ec) {
		super(id, display, isUpgraded, rarity, ec);
	}
	
	protected void setBaseProperties(int manaCost, int staminaCost) {
		this.manaCost = manaCost;
		this.staminaCost = staminaCost;
	}
	
	protected void addToLore(ArrayList<String> lore) {
		if (manaCost > 0) lore.add("<gold>Mana Cost: <yellow>" + manaCost);
		if (staminaCost > 0) lore.add("<gold>Stamina Cost: <yellow>" + staminaCost);
	}
	
	public double getManaCost() {
		return manaCost;
	}
	
	public double getStaminaCost() {
		return staminaCost;
	}
}
