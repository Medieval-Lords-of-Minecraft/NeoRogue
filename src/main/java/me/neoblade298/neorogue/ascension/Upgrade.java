package me.neoblade298.neorogue.ascension;

import java.util.HashMap;

import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;

public abstract class Upgrade {
	private static HashMap<String, Upgrade> upgrades;
	
	private String id;
	
	public Upgrade(String id) {
		this.id = id;
		upgrades.put(id, this);
	}
	
	public static Upgrade get(String id) {
		return upgrades.get(id);
	}
	
	public String getId() {
		return id;
	}
	
	public abstract void initialize(Session s, PlayerSessionData data);
}
