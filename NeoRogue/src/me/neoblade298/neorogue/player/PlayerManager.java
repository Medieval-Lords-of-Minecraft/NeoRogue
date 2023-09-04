package me.neoblade298.neorogue.player;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.io.IOComponent;

public class PlayerManager implements IOComponent {
	private static HashMap<UUID, PlayerData> data = new HashMap<UUID, PlayerData>();
	
	public PlayerManager() {
		// Strictly for debug
		for (Player p : Bukkit.getOnlinePlayers()) {
			try (Connection con = NeoCore.getConnection("NeoRogue-PlayerManager");
					Statement stmt = con.createStatement()){
				loadPlayer(p, stmt);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static PlayerData getPlayerData(UUID uuid) {
		System.out.println(data);
		return data.get(uuid);
	}
	
	public static boolean hasPlayerData(UUID uuid) {
		return data.containsKey(uuid);
	}
	
	public static String getDisplay(UUID uuid) {
		return data.get(uuid).getDisplay();
	}

	@Override
	public void cleanup(Statement p, Statement stmt) {
		
	}

	@Override
	public void loadPlayer(Player p, Statement stmt) {
		UUID uuid = p.getUniqueId();
		if (data.containsKey(uuid)) {
			data.get(uuid).updatePlayer();
			return;
		}
		
		// Check if player exists on SQL
		try {
			data.put(uuid, new PlayerData(p, stmt));
		}
		catch (Exception e) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to load player data for user " + p.getName());
			e.printStackTrace();
		}
	}

	@Override
	public void preloadPlayer(OfflinePlayer p, Statement stmt) {
		
	}

	@Override
	public void savePlayer(Player p, Statement insert, Statement delete) {
		data.get(p.getUniqueId()).save(insert);
	}

}
