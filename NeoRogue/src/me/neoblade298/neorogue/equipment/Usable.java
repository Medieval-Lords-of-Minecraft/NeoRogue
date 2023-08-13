package me.neoblade298.neorogue.equipment;

public abstract class Usable extends HotbarCompatible {
	protected int cooldown = 0;

	public Usable(String id, boolean isUpgraded, Rarity rarity, EquipmentClass ec) {
		super(id, isUpgraded, rarity, ec);
	}
	
	public int getCooldown() {
		return cooldown;
	}
}
