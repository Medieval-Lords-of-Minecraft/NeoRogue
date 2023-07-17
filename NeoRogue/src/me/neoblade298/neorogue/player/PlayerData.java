package me.neoblade298.neorogue.player;

import org.bukkit.entity.Player;

public class PlayerData {
	private int exp, level;
	private Player p;
	// Something that holds ascension tree skills
	
	// Create new one if one doesn't exist
	public PlayerData(Player p) {
		this.p = p;
	}
	
	public Player getPlayer() {
		return p;
	}
}
