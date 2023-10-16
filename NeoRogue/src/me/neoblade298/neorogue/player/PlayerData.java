package me.neoblade298.neorogue.player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.util.SQLInsertBuilder;
import me.neoblade298.neocore.shared.util.SQLInsertBuilder.SQLAction;
import me.neoblade298.neorogue.ascension.Upgrade;
import me.neoblade298.neorogue.session.Session;

public class PlayerData {
	private int exp, level;
	private UUID uuid;
	private Player p;
	private String display;
	private HashMap<String, Upgrade> upgrades = new HashMap<String, Upgrade>();
	private HashMap<Integer, SessionSnapshot> snapshots = new HashMap<Integer, SessionSnapshot>();
	private int slotsAvailable, upgradePoints;
	// Something that holds ascension tree skills
	
	// Create new one if one doesn't exist
	public PlayerData(Player p) {
		this.uuid = p.getUniqueId();
		this.p = p;
		this.display = p.getName();
		
		this.level = 1;
		this.exp = 0;
		this.upgradePoints = 0;
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
		try (Connection con = NeoCore.getConnection("NeoRogue-PlayerManager");
				Statement scnd = con.createStatement();){
			// Need a second statement since I have a nested query
			UUID uuid = p.getUniqueId();
			ResultSet base = stmt.executeQuery("SELECT * FROM neorogue_playerdata WHERE uuid = '" + uuid + "';");
			if (!base.next()) return;
			this.level = base.getInt("level");
			this.exp = base.getInt("exp");
			this.upgradePoints = base.getInt("points");
			
			ResultSet tree = stmt.executeQuery("SELECT * FROM neorogue_upgrades WHERE uuid = '" + uuid + "';");
			while (tree.next()) {
				Upgrade up = Upgrade.get(tree.getString("upgrade"));
				upgrades.put(up.getId(), up);
			}
			
			// Load snapshots
			ResultSet saves = stmt.executeQuery("SELECT * FROM neorogue_sessions WHERE host = '" + uuid + "';");
			while (saves.next()) {
				int slot = saves.getInt("slot");
				ResultSet party = scnd.executeQuery("SELECT * FROM neorogue_playersessiondata WHERE host = '" + uuid + 
						"' AND slot = " + slot + ";");
				snapshots.put(slot, new SessionSnapshot(saves, party));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Player getPlayer() {
		if (p == null) {
			p = Bukkit.getPlayer(uuid);
		}
		return p;
	}
	
	public void updatePlayer() {
		p = Bukkit.getPlayer(uuid);
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
			SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.REPLACE, "neorogue_playerdata")
					.addString(uuid.toString()).addValue(level).addValue(exp).addValue(upgradePoints)
					.addString(display);
			stmt.addBatch(sql.build());
			for (String upgrade : upgrades.keySet()) {
				stmt.addBatch("REPLACE INTO neorogue_upgrades "
						+ "VALUES ('" + uuid + "','" + upgrade + "');");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Should only ever be displayed to the owner
	public void displayLoadButtons(CommandSender s) {
		Util.msgRaw(s, "&7Save slots (Click one to load):");
		for (int i = 1; i <= slotsAvailable; i++) {
			if (snapshots.containsKey(i)) {
				snapshots.get(i).displayLoadButton(s, i);
			}
			else {
				Util.msgRaw(s, "&7&l[" + i + "] &7Empty");
			}
		}
	}
	
	// Should only ever be displayed to the owner
	public void displayNewButtons(CommandSender s) {
		Util.msgRaw(s, "&7Save slots (Click one to start a new game with):");
		for (int i = 1; i <= slotsAvailable; i++) {
			if (snapshots.containsKey(i)) {
				snapshots.get(i).displayNewButton(s, i);
			}
			else {
				SessionSnapshot.displayEmptyNewButton(s, i);
			}
		}
	}
	
	public SessionSnapshot getSnapshot(int saveSlot) {
		return snapshots.get(saveSlot);
	}
	
	public boolean hasUpgrade(String id) {
		return upgrades.containsKey(id);
	}
	
	public void addUpgrade(Upgrade up) {
		upgrades.put(up.getId(), up);
	}
	
	public int getPoints() {
		return upgradePoints;
	}
	
	public void initialize(Session s, PlayerSessionData data) {
		for (Upgrade up : upgrades.values()) {
			up.initialize(s, data);
		}
	}
}
