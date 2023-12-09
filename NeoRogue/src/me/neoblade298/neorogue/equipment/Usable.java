package me.neoblade298.neorogue.equipment;

public abstract class Usable extends HotbarCompatible {
	protected int cooldown = 0;

	public Usable(String id, String display, boolean isUpgraded, Rarity rarity, EquipmentClass ec) {
		super(id, display, isUpgraded, rarity, ec);
	}
	
	protected void setBaseProperties(int cooldown, int manaCost, int staminaCost) {
		super.setBaseProperties(manaCost, staminaCost);
		this.cooldown = cooldown;
	}
	
	public int getCooldown() {
		return cooldown;
	}
}
