package me.neoblade298.neorogue.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.area.Node;
import me.neoblade298.neorogue.player.PlayerSessionData;

public class Session {
	private AreaType areaType;
	private Area area;
	private UUID host;
	private HashMap<UUID, PlayerSessionData> party = new HashMap<UUID, PlayerSessionData>();
	private Instance inst;
	private Node curr;
	private SessionStatistics stats;
	private int xOff, zOff, nodesVisited;
	
	public Session(Player p, Plot plot) {
		this.xOff = plot.getXOffset();
		this.zOff = plot.getZOffset();
		host = p.getUniqueId();
		party.put(p.getUniqueId(), new PlayerSessionData(p.getUniqueId()));
		
		area = new Area(AreaType.LOW_DISTRICT, xOff, zOff, this);
		area.generate();
		// Strictly Debug
		area.update(area.getNodes()[1][2]);
		
		this.inst = new LobbyInstance();
	}
	
	public HashMap<UUID, PlayerSessionData> getParty() {
		return party;
	}
	
	public Instance getInstance() {
		return inst;
	}
	
	public void setInstance(Instance inst) {
		this.inst.cleanup();
		this.inst = inst;
		inst.start(this);
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
}
