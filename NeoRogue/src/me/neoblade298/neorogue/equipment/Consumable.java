package me.neoblade298.neorogue.equipment;

public abstract class Consumable extends Usable {

	public Consumable(String id, String display, boolean isUpgraded, Rarity rarity, EquipmentClass ec) {
		super(id, display, isUpgraded, rarity, ec);
	}

}
