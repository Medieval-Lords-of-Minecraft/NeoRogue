package me.neoblade298.neorogue.equipment;

public enum Rarity {
	COMMON(0, "Common", "&7"),
	UNCOMMON(2, "Uncommon", "&a"),
	RARE(4, "Rare", "&9"),
	EPIC(6, "Epic", "&6"),
	LEGENDARY(8, "Legendary", "&4&l");
	
	private int value;
	private String display;
	private String color;
	
	private Rarity(int value, String display, String color) {
		this.value = value;
		this.display = display;
		this.color = color.replaceAll("&", "§");
	}
	
	public int getValue() {
		return value;
	}
	
	public String getDisplay(boolean withColor) {
		return (withColor ? color : "") + display;
	}
	
	public String getColor() {
		return color;
	}
}
