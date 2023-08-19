package me.neoblade298.neorogue.session.fights;

import net.md_5.bungee.api.ChatColor;

public enum Amount {
	NONE(ChatColor.GRAY, "None"),
	LIGHT(ChatColor.YELLOW, "Light"),
	MEDIUM(ChatColor.GOLD, "Medium"),
	HEAVY(ChatColor.RED, "Heavy");
	
	private ChatColor color;
	private String display;
	private Amount(ChatColor color, String display) {
		this.color = color;
		this.display = display;
	}
	
	public String getDisplay(boolean hasColor) {
		return (hasColor ? color : "") + display;
	}
}
