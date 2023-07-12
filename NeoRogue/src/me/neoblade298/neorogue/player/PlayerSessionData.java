package me.neoblade298.neorogue.player;

import java.util.UUID;

import org.bukkit.entity.Player;

public class PlayerSessionData {
	private PlayerData data;
	private double health, mana, stamina;
	
	public PlayerSessionData(UUID uuid) {
		data = PlayerManager.getPlayerData(uuid);
		health = 100;
		stamina = 100;
		mana = 100;
	}
}
