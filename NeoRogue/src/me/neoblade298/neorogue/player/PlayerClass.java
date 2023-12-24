package me.neoblade298.neorogue.player;

import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;

public enum PlayerClass {
	WARRIOR("Warrior", EquipmentClass.WARRIOR),
	THIEF("Thief", EquipmentClass.THIEF),
	ARCHER("Archer", EquipmentClass.ARCHER),
	MAGE("Mage", EquipmentClass.MAGE);
	
	private String display;
	private EquipmentClass ec;
	private PlayerClass(String display, EquipmentClass ec) {
		this.display = display;
		this.ec = ec;
	}
	
	public EquipmentClass toEquipmentClass() {
		return ec;
	}
	
	public String getDisplay() {
		return display;
	}
}
