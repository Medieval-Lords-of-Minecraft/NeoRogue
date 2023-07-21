package me.neoblade298.neorogue.player;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerData {
	private int exp, level;
	private UUID uuid;
	private Player p;
	// Something that holds ascension tree skills
	
	// Create new one if one doesn't exist
	public PlayerData(Player p) {
		this.uuid = p.getUniqueId();
		this.p = p;
	}
	
	public Player getPlayer() {
		if (p == null) {
			Bukkit.getPlayer(uuid);
		}
		return p;
	}
}
