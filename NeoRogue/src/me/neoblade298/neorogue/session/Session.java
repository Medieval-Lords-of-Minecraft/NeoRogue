package me.neoblade298.neorogue.session;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.io.SQLManager;
import me.neoblade298.neocore.shared.util.SQLInsertBuilder;
import me.neoblade298.neocore.shared.util.SQLInsertBuilder.SQLAction;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.area.Node;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Session {
	private Area area;
	private UUID host;
	private HashMap<UUID, PlayerSessionData> party = new HashMap<UUID, PlayerSessionData>();
	private Instance inst;
	private Node curr;
	private SessionStatistics stats;
	private int saveSlot, xOff, zOff, nodesVisited = 12, areasCompleted;
	private Plot plot;
	
	public Session(Player p, Plot plot, String lobby, int saveSlot) {
		this.saveSlot = saveSlot;
		this.xOff = plot.getXOffset();
		this.zOff = plot.getZOffset();
		host = p.getUniqueId();
		this.plot = plot;
		this.inst = new LobbyInstance(lobby, p, this);
	}
	
	// Load from existing data
	public Session(Player p, Plot plot, int saveSlot) {
		this.saveSlot = saveSlot;
		this.xOff = plot.getXOffset();
		this.zOff = plot.getZOffset();
		host = p.getUniqueId();
		this.plot = plot;
		
		Session s = this;
		new BukkitRunnable() {
			public void run() {
				Util.msgRaw(p, Component.text("Loading game...", NamedTextColor.GRAY));
				
				try (Connection con = SQLManager.getConnection("NeoRogue");
						Statement stmt = con.createStatement()) {
					ResultSet partySet = stmt.executeQuery("SELECT * FROM neorogue_playersessiondata WHERE host = '" + host + "' AND slot = " + saveSlot + ";");
					while (partySet.next()) {
						UUID uuid = UUID.fromString(partySet.getString("uuid"));
						party.put(uuid, new PlayerSessionData(uuid, s, partySet));
					}
					
					ResultSet sessSet = stmt.executeQuery("SELECT * FROM neorogue_sessions WHERE host = '" + host + "' AND slot = " + saveSlot + ";");
					sessSet.next();
					nodesVisited = sessSet.getInt("nodesVisited");
					int pos = sessSet.getInt("position");
					int lane = sessSet.getInt("lane");
					Instance inst = Instance.deserialize(sessSet, party);

					area = new Area(AreaType.valueOf(sessSet.getString("areaType")),
							xOff, zOff, host, saveSlot, s, stmt);
					curr = area.getNodes()[pos][lane];
					
					new BukkitRunnable() {
						public void run() {
							area.instantiate();
							setInstance(inst);
							Util.msgRaw(p, Component.text("Finished loading.", NamedTextColor.GRAY));
						}
					}.runTask(NeoRogue.inst());
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}
	
	public void save(Statement insert, Statement delete) {
		if (inst instanceof FightInstance || inst instanceof LoseInstance) return;
		
		try {
			SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.REPLACE, "neorogue_sessions")
					.addString(host.toString()).addValue(saveSlot).addString(area.getType().name())
					.addValue(curr.getPosition()).addValue(curr.getLane()).addValue(nodesVisited)
					.addValue(System.currentTimeMillis()).addString(inst.serialize(party));
					insert.execute(sql.build());
		}
		catch (SQLException ex) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to save session for host " + host + " to slot " + saveSlot);
			ex.printStackTrace();
		}
		
		// Only save the nodes near the player
		area.saveRelevant(insert, delete);
		
		try {
			delete.execute("DELETE FROM neorogue_playersessiondata WHERE host = '" + host + "' AND slot = " + saveSlot + ";");
			for (PlayerSessionData psd : party.values()) {
				psd.save(insert);
			}
		}
		catch (SQLException ex) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to save player session data for host " + host + " to slot " + saveSlot);
			ex.printStackTrace();
		}
		
		party.get(host).getData().updateSnapshot(this, saveSlot);
	}
	
	public void addPlayers(HashMap<UUID, EquipmentClass> players) {
		for (Entry<UUID, EquipmentClass> ent : players.entrySet()) {
			party.put(ent.getKey(), new PlayerSessionData(ent.getKey(), ent.getValue(), this));
		}
	}
	
	public void addPlayer(UUID uuid, EquipmentClass pc) {
		party.put(uuid, new PlayerSessionData(uuid, pc, this));
	}

	public void broadcast(String msg) {
		for (Player p : getOnlinePlayers()) {
			Util.msgRaw(p, NeoCore.miniMessage().deserialize(msg).colorIfAbsent(NamedTextColor.GRAY));
		}
	}

	public void broadcast(Component msg) {
		for (Player p : getOnlinePlayers()) {
			Util.msgRaw(p, msg.colorIfAbsent(NamedTextColor.GRAY));
		}
	}
	
	public void broadcastSound(Sound s) {
		for (Player p : getOnlinePlayers()) {
			p.playSound(p, s, 1F, 1F);
		}
	}
	
	public HashMap<UUID, PlayerSessionData> getParty() {
		return party;
	}
	
	public Instance getInstance() {
		return inst;
	}
	
	public boolean isEveryoneOnline() {
		for (UUID uuid : party.keySet()) {
			if (Bukkit.getPlayer(uuid) == null) {
				broadcast("<red>You can't move on until every member in your party is online!");
				return false;
			}
		}
		return true;
	}
	
	public void setInstance(Instance inst) {
		boolean firstLoad = this.inst == null;
		if (!firstLoad) {
			if (!(this.inst instanceof FightInstance)) {
				for (UUID uuid : party.keySet()) {
					if (Bukkit.getPlayer(uuid) == null) {
						broadcast("<red>You can't move on until every member in your party is online!");
						return;
					}
				}
			}
			
			// Save player's storage
			if (this.inst instanceof EditInventoryInstance && !EditInventoryInstance.isValid(this)) return;
			this.inst.cleanup();
		}
		this.inst = inst;
		inst.start(this);
		if (inst instanceof EditInventoryInstance) {
			for (PlayerSessionData data : party.values()) {
				data.setupInventory();
				data.setupEditInventory(); // hunger and exp bar
			}
		}
		
		// Auto-save
		if (firstLoad) return;
		new BukkitRunnable() {
			public void run() {
				try (Connection con = SQLManager.getConnection("NeoRogue");
						Statement insert = con.createStatement();
						Statement delete = con.createStatement()){
					save(insert, delete);
				} catch (SQLException ex) {
					Bukkit.getLogger().warning("[NeoRogue] Failed to acquire connection to save session hosted by " + host + " to slot " + saveSlot);
					ex.printStackTrace();
				}
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}
	
	public PlayerSessionData getData(UUID uuid) {
		return party.get(uuid);
	}
	
	public Area getArea() {
		return area;
	}
	
	public ArrayList<Player> getOnlinePlayers() {
		ArrayList<Player> players = new ArrayList<Player>();
		for (UUID uuid : party.keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			if (p != null) players.add(p);
		}
		return players;
	}
	
	public int getNodesVisited() {
		return nodesVisited;
	}
	
	public int getXOff() {
		return xOff;
	}
	
	public int getZOff() {
		return zOff;
	}
	
	public Plot getPlot() {
		return plot;
	}
	
	public void generateArea(AreaType type) {
		this.area = new Area(type, xOff, zOff, this);
		area.instantiate();
	}
	
	public Node getNode() {
		return curr;
	}
	
	public void setNode(Node node) {
		this.curr = node;
	}
	
	public UUID getHost() {
		return host;
	}
	
	public SessionStatistics getStats() {
		return stats;
	}
	
	public int getAreasCompleted() {
		return areasCompleted;
	}
	
	public int getSaveSlot() {
		return saveSlot;
	}
	
	public void teleportToInstance(Player p) {
		inst.teleportPlayer(p);
	}
	
	public void cleanup() {
		inst.cleanup();
	}
	
	public void leavePlayer(Player p) {
		UUID uuid = p.getUniqueId();
		if (uuid.equals(host)) {
			broadcast("The host has left the party, so the game has ended!");
			SessionManager.endSession(this);
		}
		else {
			broadcast("<yellow>" + p.getName() + " <gray>has left the party!");
			party.remove(p.getUniqueId());
			SessionManager.removeFromSession(p.getUniqueId());
		}
	}
	
	public void kickPlayer(Player p) {
		UUID uuid = p.getUniqueId();
		if (!uuid.equals(host)) {
			Util.displayError(p, "Only the host may kick players");
		}
		else {
			broadcast("<yellow>" + p.getName() + " <gray>was kicked from the party!");
			party.remove(p.getUniqueId());
			SessionManager.removeFromSession(p.getUniqueId());
		}
	}
	
	public void deleteSave() {
		try (Connection con = SQLManager.getConnection("NeoRogue");
				Statement stmt = con.createStatement()){
			stmt.executeUpdate("DELETE FROM neorogue_playersessiondata WHERE host = '" + host + "' AND slot = " + saveSlot + ";");
			stmt.executeUpdate("DELETE FROM neorogue_sessions WHERE host = '" + host + "' AND slot = " + saveSlot + ";");
			stmt.executeUpdate("DELETE FROM neorogue_sessions WHERE host = '" + host + "' AND slot = " + saveSlot + ";");
		} catch (SQLException ex) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to acquire connection to delete session hosted by " + host + " from slot " + saveSlot);
			ex.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		return plot.toString();
	}
}
