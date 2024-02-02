package me.neoblade298.neorogue.equipment;

public abstract class Consumable extends Equipment {
	public Consumable(String id, String display, boolean isUpgraded, Rarity rarity, EquipmentClass ec) {
		super(id, display, isUpgraded, rarity, ec, EquipmentType.CONSUMABLE, EquipmentProperties.none());
	}
}
