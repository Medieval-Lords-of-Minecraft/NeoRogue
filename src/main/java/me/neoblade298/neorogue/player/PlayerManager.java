package me.neoblade298.neorogue.player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.io.IOComponent;

public class PlayerManager implements IOComponent {
	private static HashMap<UUID, PlayerData> data = new HashMap<UUID, PlayerData>();
	
	public PlayerManager() {
		try (Connection con = NeoCore.getConnection("NeoRogue-PlayerManager");
				Statement stmt = con.createStatement()) {
			stmt.execute("CREATE TABLE IF NOT EXISTS neorogue_unlocknodes (uuid VARCHAR(36) NOT NULL, node VARCHAR(100) NOT NULL, PRIMARY KEY (uuid, node));");
		}
		catch (SQLException e) {
			e.printStackTrace();
		}

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
		return data.get(uuid);
	}
	
	public static boolean hasPlayerData(UUID uuid) {
		return data.containsKey(uuid);
	}

	public static void initializeEquipmentDroptables() {
		for (PlayerData pd : data.values()) {
			pd.initializeEquipmentDroptable();
		}
	}
	
	public static String getDisplay(UUID uuid) {
		return data.get(uuid).getDisplay();
	}

	@Override
	public void cleanup(Connection con, List<PreparedStatement> stmts) {
		
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
	public void savePlayer(Player p, Connection con, List<PreparedStatement> stmts) throws Exception {
		data.get(p.getUniqueId()).save(con, stmts);
	}

}
