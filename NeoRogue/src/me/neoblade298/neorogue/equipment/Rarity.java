package me.neoblade298.neorogue.equipment;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public enum Rarity {
	COMMON(0, "Common", NamedTextColor.GRAY, null),
	UNCOMMON(2, "Uncommon", NamedTextColor.GREEN, null),
	RARE(4, "Rare", NamedTextColor.BLUE, null),
	EPIC(6, "Epic", NamedTextColor.GOLD, null),
	LEGENDARY(8, "Legendary", NamedTextColor.RED, TextDecoration.BOLD);
	
	private int value;
	private Component display;
	private NamedTextColor color;
	private TextDecoration decor;
	
	private Rarity(int value, String display, NamedTextColor color, TextDecoration decor) {
		this.value = value;
		this.display = Component.text(display);
		this.color = color;
		this.decor = decor;
	}
	
	public int getValue() {
		return value;
	}
	
	public Component getDisplay(boolean withColor) {
		if (withColor) {
			Component c = display.color(color);
			if (decor != null) c = c.decorate(decor);
			return c;
		}
		return display;
	}
	
	public NamedTextColor getColor() {
		return color;
	}
	
	public TextDecoration getDecoration() {
		return decor;
	}
	
	public Component applyDecorations(Component c) {
		c = c.colorIfAbsent(color);
		if (decor != null) c = c.decorate(decor);
		return c;
	}
}
