package me.neoblade298.neorogue.ascension;

import java.util.ArrayList;

import me.neoblade298.neorogue.player.PlayerData;

public class UpgradeRequirement {
	private ArrayList<String> ands = new ArrayList<String>();
	private ArrayList<ArrayList<String>> ors = new ArrayList<ArrayList<String>>();
	
	public UpgradeRequirement(String... upgrades) {
		for (String upgrade : upgrades) {
			ands.add(upgrade);
		}
	}
	
	public void addOr(ArrayList<String> or) {
		ors.add(or);
	}
	
	public void addAnd(String... ands) {
		for (String and : ands) {
			this.ands.add(and);
		}
	}
	
	public boolean passesRequirement(PlayerData data) {
		for (String and : ands) {
			if (!data.hasUpgrade(and)) return false;
		}
		
		for (ArrayList<String> or : ors) {
			boolean passes = false;
			for (String o : or) {
				if (data.hasUpgrade(o)) {
					passes = true;
					break;
				}
			}
			if (!passes) return false;
		}
		return true;
	}
}
