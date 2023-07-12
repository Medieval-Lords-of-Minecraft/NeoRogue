package me.neoblade298.neorogue.session;

import java.util.ArrayList;
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
	private ArrayList<PlayerSessionData> party = new ArrayList<PlayerSessionData>();
	private Instance inst;
	private Node curr;
	private SessionStatistics stats;
	
	
	public Session(Player p) {
		host = p.getUniqueId();
		party.add(new PlayerSessionData(p.getUniqueId()));
	}
}
