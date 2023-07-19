package me.neoblade298.neorogue.player;

import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.io.IOComponent;

public class PlayerManager implements IOComponent {
	private static HashMap<UUID, PlayerData> data = new HashMap<UUID, PlayerData>();
	
	public PlayerManager() {
		// Strictly for debug
		for (Player p : Bukkit.getOnlinePlayers()) {
			data.put(p.getUniqueId(), new PlayerData(p));
		}
	}

	public static PlayerData getPlayerData(UUID uuid) {
		return data.get(uuid);
	}

	@Override
	public void cleanup(Statement p, Statement stmt) {
		
	}

	@Override
	public void loadPlayer(Player p, Statement stmt) {
		data.putIfAbsent(p.getUniqueId(), new PlayerData(p));
	}

	@Override
	public void preloadPlayer(OfflinePlayer p, Statement stmt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void savePlayer(Player arg0, Statement arg1, Statement arg2) {
		// TODO Auto-generated method stub
		
	}

}
