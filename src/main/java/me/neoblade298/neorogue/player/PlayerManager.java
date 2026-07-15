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
import me.neoblade298.neorogue.player.boost.GlobalBoostManager;

public class PlayerManager implements IOComponent {
	private static HashMap<UUID, PlayerData> data = new HashMap<UUID, PlayerData>();
	
	public PlayerManager() {
		try (Connection con = NeoCore.getConnection("NeoRogue-PlayerManager");
				Statement stmt = con.createStatement()) {
			stmt.execute("CREATE TABLE IF NOT EXISTS neorogue_unlocknodes (uuid VARCHAR(36) NOT NULL, node VARCHAR(100) NOT NULL, PRIMARY KEY (uuid, node));");
			try {
				stmt.execute("CREATE INDEX idx_neorogue_unlocknodes_uuid ON neorogue_unlocknodes (uuid);");
			} catch (SQLException ignore) {
				// Index already exists
			}
			stmt.execute("CREATE TABLE IF NOT EXISTS neorogue_achievements (uuid VARCHAR(36) NOT NULL, achievement VARCHAR(100) NOT NULL, progress INT NOT NULL DEFAULT 0, scope VARCHAR(40) NOT NULL DEFAULT 'GLOBAL', data TEXT, PRIMARY KEY (uuid, achievement, scope));");
			stmt.execute("CREATE TABLE IF NOT EXISTS neorogue_expboosts (uuid VARCHAR(36) NOT NULL, type VARCHAR(64) NOT NULL, remaining BIGINT NOT NULL, PRIMARY KEY (uuid, type));");
			stmt.execute("CREATE TABLE IF NOT EXISTS neorogue_global_expboosts (type VARCHAR(64) NOT NULL, remaining BIGINT NOT NULL, PRIMARY KEY (type));");
			stmt.execute("CREATE TABLE IF NOT EXISTS neorogue_playercargo (uuid VARCHAR(36) NOT NULL, material VARCHAR(64) NOT NULL, amount INT NOT NULL, PRIMARY KEY (uuid, material));");
			stmt.execute("CREATE TABLE IF NOT EXISTS neorogue_playercargo_meta (uuid VARCHAR(36) NOT NULL, capacity INT NOT NULL DEFAULT 3000, slots INT NOT NULL DEFAULT 5, PRIMARY KEY (uuid));");
			stmt.execute("CREATE TABLE IF NOT EXISTS neorogue_sessioncargo (host VARCHAR(36) NOT NULL, slot INT NOT NULL, uuid VARCHAR(36) NOT NULL, material VARCHAR(64) NOT NULL, amount INT NOT NULL, PRIMARY KEY (host, slot, uuid, material));");
			stmt.execute("CREATE TABLE IF NOT EXISTS neorogue_sessioncargosold (host VARCHAR(36) NOT NULL, slot INT NOT NULL, uuid VARCHAR(36) NOT NULL, material VARCHAR(64) NOT NULL, amount INT NOT NULL, value DOUBLE NOT NULL, PRIMARY KEY (host, slot, uuid, material));");
			stmt.execute("CREATE TABLE IF NOT EXISTS neorogue_playerlostcargo (uuid VARCHAR(36) NOT NULL, material VARCHAR(64) NOT NULL, amount INT NOT NULL, PRIMARY KEY (uuid, material));");

			// Core player tables
			stmt.execute("CREATE TABLE IF NOT EXISTS neorogue_playerdata (uuid VARCHAR(36) NOT NULL, display VARCHAR(255), PRIMARY KEY (uuid));");
			stmt.execute("CREATE TABLE IF NOT EXISTS neorogue_playerflags (uuid VARCHAR(36) NOT NULL, flag VARCHAR(100) NOT NULL, PRIMARY KEY (uuid, flag));");
			stmt.execute("CREATE TABLE IF NOT EXISTS neorogue_playerclass (uuid VARCHAR(36) NOT NULL, class VARCHAR(40) NOT NULL, level INT NOT NULL DEFAULT 1, exp INT NOT NULL DEFAULT 0, points INT NOT NULL DEFAULT 0, notoriety_max INT NOT NULL DEFAULT 0, PRIMARY KEY (uuid, class));");

			// Session save tables
			stmt.execute("CREATE TABLE IF NOT EXISTS neorogue_sessions (host VARCHAR(36) NOT NULL, slot INT NOT NULL, regionType VARCHAR(50), position INT, lane INT, nodesVisited INT, regionsCompleted INT, potionChance INT, notoriety INT, endless TINYINT, lastSaved BIGINT, instanceData TEXT, sessionType VARCHAR(40), lastMiniboss VARCHAR(100), PRIMARY KEY (host, slot));");
			stmt.execute("CREATE TABLE IF NOT EXISTS neorogue_nodes (host VARCHAR(36) NOT NULL, slot INT NOT NULL, type VARCHAR(40), position INT NOT NULL, lane INT NOT NULL, destinations TEXT, instanceData TEXT, PRIMARY KEY (host, slot, position, lane));");
			stmt.execute("CREATE TABLE IF NOT EXISTS neorogue_playersessiondata ("
					+ "host VARCHAR(36) NOT NULL, slot INT NOT NULL, uuid VARCHAR(36) NOT NULL, display VARCHAR(255),"
					+ " playerClass VARCHAR(40), maxHealth DOUBLE, maxMana DOUBLE, maxStamina DOUBLE, health DOUBLE,"
					+ " startingMana DOUBLE, startingStamina DOUBLE, manaRegen DOUBLE, staminaRegen DOUBLE,"
					+ " hotbar TEXT, armors TEXT, offhand TEXT, accessories TEXT, storage TEXT, otherBinds TEXT, artifacts TEXT,"
					+ " maxAbilities INT, maxStorage INT, armorSlots INT, accessorySlots INT, coins INT, instanceData TEXT,"
					+ " statDamageDealt DOUBLE, statDamageTakenHealth DOUBLE, statDamageTakenShields DOUBLE,"
					+ " statShieldsApplied DOUBLE, statHealingDone DOUBLE, statDamageBarriered DOUBLE,"
					+ " statFightsCompleted INT, statDeaths INT, statStatusesApplied INT, statDmgHealthRegionStart DOUBLE,"
					+ " runExpBoostMultiplier DOUBLE, PRIMARY KEY (host, slot, uuid));");
		}
		catch (SQLException e) {
			e.printStackTrace();
		}

		GlobalBoostManager.load();

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
