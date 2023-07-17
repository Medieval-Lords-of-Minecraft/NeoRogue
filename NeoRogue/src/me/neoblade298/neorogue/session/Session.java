package me.neoblade298.neorogue.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import com.sk89q.worldedit.entity.Player;

import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.area.Node;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.instance.Instance;

public class Session {
	private AreaType areaType;
	private Area area;
	private UUID host;
	private HashMap<UUID, PlayerSessionData> party = new HashMap<UUID, PlayerSessionData>();
	private Instance inst;
	private Node curr;
	private SessionStatistics stats;
	
	
	public Session(Player p) {
		host = p.getUniqueId();
		party.put(p.getUniqueId(), new PlayerSessionData(p.getUniqueId()));
	}
	
	public Instance getInstance() {
		return inst;
	}
	
	public PlayerSessionData getData(UUID uuid) {
		return party.get(uuid);
	}
}
