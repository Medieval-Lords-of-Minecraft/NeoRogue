package me.neoblade298.neorogue.equipment;

public abstract class Consumable extends Equipment {
	public Consumable(String id, String display, Rarity rarity, EquipmentClass ec) {
		super(id, display, false, rarity, ec, EquipmentType.CONSUMABLE, EquipmentProperties.none());
	}
}
