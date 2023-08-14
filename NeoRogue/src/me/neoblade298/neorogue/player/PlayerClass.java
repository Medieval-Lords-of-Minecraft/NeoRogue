package me.neoblade298.neorogue.player;

public enum PlayerClass {
	SWORDSMAN("Swordsman"),
	THIEF("Thief"),
	ARCHER("Archer"),
	MAGE("Mage");
	
	private String display;
	private PlayerClass(String display) {
		this.display = display;
	}
	
	public String getDisplay() {
		return display;
	}
}
