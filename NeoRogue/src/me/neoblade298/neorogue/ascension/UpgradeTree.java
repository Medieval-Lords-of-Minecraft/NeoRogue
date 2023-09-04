package me.neoblade298.neorogue.ascension;

import java.util.HashMap;

import me.neoblade298.neorogue.player.PlayerData;

public abstract class UpgradeTree {
	private static HashMap<String, UpgradeTree> trees = new HashMap<String, UpgradeTree>();
	
	private String id, display, description;
	private int slot;
	private HashMap<Integer, UpgradeHolder> upgrades;
	private UpgradeRequirement req;
	
	public UpgradeTree(String id, String display, String description, int slot) {
		this.id = id;
		this.display = display;
		this.description = description;
		this.slot = slot;
		
		trees.put(id, this);
	}
	
	public boolean passesRequirement(PlayerData data) {
		return req.passesRequirement(data);
	}
}
