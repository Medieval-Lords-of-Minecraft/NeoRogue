package me.neoblade298.neorogue.session.fights;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public enum Amount {
	NONE(NamedTextColor.GRAY, "None"),
	LIGHT(NamedTextColor.YELLOW, "Light"),
	MEDIUM(NamedTextColor.GOLD, "Medium"),
	HEAVY(NamedTextColor.RED, "Heavy");
	
	private NamedTextColor color;
	private String display;
	private Amount(NamedTextColor color, String display) {
		this.color = color;
		this.display = display;
	}
	
	public Component getDisplay(boolean hasColor) {
		Component c = Component.text(display);
		return hasColor ? c.color(color) : c;
	}
}
