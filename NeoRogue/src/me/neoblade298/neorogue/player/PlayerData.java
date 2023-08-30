package me.neoblade298.neorogue.player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.util.SharedUtil;

public class PlayerData {
	private int exp, level;
	private UUID uuid;
	private Player p;
	private String display;
	private HashMap<Integer, SessionSnapshot> snapshots;
	// Something that holds ascension tree skills
	
	// Create new one if one doesn't exist
	public PlayerData(Player p) {
		this.uuid = p.getUniqueId();
		this.p = p;
		this.display = p.getName();
		this.snapshots = new HashMap<Integer, SessionSnapshot>();
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
		Util.msgRaw(p, "&7Save files (Click one to load):");
		for (int i = 1; i <= snapshots.size(); i++) {
			snapshots.get(i).displayLoadButton(p, i);
		}
	}
	
	// Should only ever be displayed to the owner
	public void displaySaveButtons() {
		Util.msgRaw(p, "&7Save files (Click one to overwrite):");
		int i;
		for (i = 1; i <= snapshots.size(); i++) {
			snapshots.get(i).displaySaveButton(p, i);
		}
		p.spigot().sendMessage(SharedUtil.createText("&7&l[" + ++i + "] &7&oClick here to create new save").create());
	}
}
