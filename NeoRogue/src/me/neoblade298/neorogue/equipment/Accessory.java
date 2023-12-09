package me.neoblade298.neorogue.equipment;

public abstract class Accessory extends Equipment {

	public Accessory(String id, String display, boolean isUpgraded, Rarity rarity, EquipmentClass ec) {
		super(id, display, isUpgraded, rarity, ec);
	}

}
