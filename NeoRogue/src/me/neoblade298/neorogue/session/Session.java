package me.neoblade298.neorogue.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.area.Node;
import me.neoblade298.neorogue.player.PlayerClass;
import me.neoblade298.neorogue.player.PlayerSessionData;

public class Session {
	private Area area;
	private UUID host;
	private HashMap<UUID, PlayerSessionData> party = new HashMap<UUID, PlayerSessionData>();
	private Instance inst;
	private Node curr;
	private SessionStatistics stats;
	private int xOff, zOff, nodesVisited, areasCompleted;
	private Plot plot;
	
	public Session(Player p, Plot plot, String lobby) {
		this.xOff = plot.getXOffset();
		this.zOff = plot.getZOffset();
		host = p.getUniqueId();
		this.plot = plot;
		this.inst = new LobbyInstance(lobby, p, this);
	}
	
	public void addPlayers(HashMap<UUID, PlayerClass> players) {
		for (Entry<UUID, PlayerClass> ent : players.entrySet()) {
			party.put(ent.getKey(), new PlayerSessionData(ent.getKey(), ent.getValue(), this));
		}
	}
	
	public void addPlayer(UUID uuid, PlayerClass pc) {
		party.put(uuid, new PlayerSessionData(uuid, pc, this));
	}

	public void broadcast(String msg) {
		for (Player p : getOnlinePlayers()) {
			Util.msgRaw(p, msg);
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
	
	public void setInstance(Instance inst) {
		if (inst instanceof EditInventoryInstance) {
			for (PlayerSessionData data : party.values()) {
				if (!data.saveStorage()) {
					for (Player online : getOnlinePlayers()) {
						Util.displayError(online, "&&4" + data.getData().getDisplay() + "&c has too many items in their inventory! They must drop some "
								+ "to satisfy their storage limit of &e" + data.getMaxStorage() + "&c!");
					}
					return;
				}
			}
		}
		this.inst.cleanup();
		this.inst = inst;
		inst.start(this);
		if (inst instanceof EditInventoryInstance) {
			for (PlayerSessionData data : party.values()) {
				data.setupInventory();
			}
		}
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
}
