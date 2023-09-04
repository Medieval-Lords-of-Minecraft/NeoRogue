package me.neoblade298.neorogue.player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.session.Session;

public class PlayerData {
	private int exp, level;
	private UUID uuid;
	private Player p;
	private String display;
	private HashSet<String> upgrades;
	private HashMap<Integer, SessionSnapshot> snapshots;
	private int slotsAvailable;
	// Something that holds ascension tree skills
	
	// Create new one if one doesn't exist
	public PlayerData(Player p) {
		this.uuid = p.getUniqueId();
		this.p = p;
		this.display = p.getName();
		this.snapshots = new HashMap<Integer, SessionSnapshot>();
		
		if (p.hasPermission("donator.diamond")) {
			slotsAvailable = 5;
		}
		else if (p.hasPermission("donator.emerald")) {
			slotsAvailable = 3;
		}
		else if (p.hasPermission("donator.sapphire")) {
			slotsAvailable = 2;
		}
		else {
			slotsAvailable = 1;
		}
	}
	
	public PlayerData(Player p, Statement stmt) {
		this(p);
		try {
			ResultSet base = stmt.executeQuery("SELECT * FROM neorogue_playerdata WHERE uuid = '" + p.getUniqueId() + "';");
			ResultSet tree = stmt.executeQuery("SELECT * FROM neorogue_ascensions WHERE uuid = '" + p.getUniqueId() + "';");
			
			// Load snapshots
			ResultSet saves = stmt.executeQuery("SELECT * FROM neorogue_sessions WHERE uuid = '" + p.getUniqueId() + "';");
			while (saves.next()) {
				int slot = saves.getInt("slot");
				ResultSet party = stmt.executeQuery("SELECT * FROM neorogue_playersessiondata WHERE host = '" + p.getUniqueId() + 
						"' AND slot = " + slot + ";");
				snapshots.put(saves.getInt("slot"), new SessionSnapshot(saves, party));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Player getPlayer() {
		if (p == null) {
			Bukkit.getPlayer(uuid);
		}
		return p;
	}
	
	public String getDisplay() {
		return display;
	}
	
	public int getLevel() {
		return level;
	}
	
	public int getExp() {
		return exp;
	}
	
	public void updateSnapshot(Session s, int saveSlot) {
		snapshots.put(saveSlot, new SessionSnapshot(s));
	}
	
	public void save(Statement stmt) {
		// Only saves player data and ascension tree, session saving is handled elsewhere
		try {
			stmt.addBatch("REPLACE INTO neorogue_playerdata "
					+ "VALUES ('" + uuid + "'," + level + "," + exp + ",'" + display + "');");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Should only ever be displayed to the owner
	public void displayLoadButtons() {
		Util.msgRaw(p, "&7Save slots (Click one to load):");
		for (int i = 1; i <= slotsAvailable; i++) {
			if (snapshots.containsKey(i)) {
				snapshots.get(i).displayLoadButton(p, i);
			}
			else {
				Util.msgRaw(p, "&7&l[" + i + "] &7Empty");
			}
		}
	}
	
	// Should only ever be displayed to the owner
	public void displayNewButtons() {
		Util.msgRaw(p, "&7Save slots (Click one to start a new game with):");
		for (int i = 1; i <= slotsAvailable; i++) {
			if (snapshots.containsKey(i)) {
				snapshots.get(i).displayNewButton(p, i);
			}
			else {
				SessionSnapshot.displayEmptyNewButton(p, i);
			}
		}
	}
	
	public SessionSnapshot getSnapshot(int saveSlot) {
		return snapshots.get(saveSlot);
	}
	
	public boolean hasUpgrade(String id) {
		return upgrades.contains(id);
	}
	
	public void addUpgrade(String id) {
		upgrades.add(id);
	}
}
