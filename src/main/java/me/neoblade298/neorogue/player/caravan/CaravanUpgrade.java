package me.neoblade298.neorogue.player.caravan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.neoblade298.neocore.shared.io.Section;
import me.neoblade298.neorogue.player.PlayerData;

// A purchasable caravan upgrade: a title, description, VaultUnlocked cost, an optional GUI slot,
// optional prerequisite upgrades, and the list of actions applied on purchase.
public class CaravanUpgrade {
	private final String id;
	private final String title;
	private final List<String> description;
	private final double cost;
	private final int slot;
	// Prerequisite upgrades, mirroring the unlock system: outer list = AND, inner array = OR
	// (pipe-separated alternatives). All groups must be satisfied; any alternative satisfies a group.
	private final List<String[]> requires;
	private final List<CaravanAction> actions = new ArrayList<CaravanAction>();

	public CaravanUpgrade(String id, Section sec) {
		this.id = id;
		this.title = sec.getString("title", id);
		List<String> desc = sec.getStringList("description");
		this.description = desc != null ? desc : new ArrayList<String>();
		this.cost = sec.getInt("cost", 0);
		this.slot = sec.getInt("slot", -1);
		List<String> reqs = sec.getStringList("requires");
		if (reqs != null && !reqs.isEmpty()) {
			ArrayList<String[]> parsed = new ArrayList<String[]>();
			for (String entry : reqs) {
				String[] orGroup = entry.split("\\s*\\|\\s*");
				for (int i = 0; i < orGroup.length; i++) {
					orGroup[i] = orGroup[i].trim();
				}
				parsed.add(orGroup);
			}
			this.requires = Collections.unmodifiableList(parsed);
		} else {
			this.requires = Collections.emptyList();
		}
		List<String> acts = sec.getStringList("actions");
		if (acts != null) {
			for (String a : acts) {
				CaravanAction action = CaravanAction.parse(a);
				if (action != null) actions.add(action);
			}
		}
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public List<String> getDescription() {
		return description;
	}

	public double getCost() {
		return cost;
	}

	public int getSlot() {
		return slot;
	}

	// Prerequisite groups: outer = AND, inner = OR.
	public List<String[]> getRequires() {
		return requires;
	}

	public List<CaravanAction> getActions() {
		return actions;
	}

	// All prerequisite groups satisfied (AND), where any alternative in a group satisfies it (OR).
	public boolean requirementsMet(PlayerData pd) {
		for (String[] orGroup : requires) {
			boolean anyMet = false;
			for (String req : orGroup) {
				if (pd.hasPurchasedUpgrade(req)) {
					anyMet = true;
					break;
				}
			}
			if (!anyMet) return false;
		}
		return true;
	}

	// Applies every action, then records the upgrade as purchased for this player.
	public void apply(PlayerData pd) {
		applyActions(pd);
		pd.addPurchasedUpgrade(id);
	}

	// Applies this upgrade's effects without marking it purchased. Used by recomputeCaravanState() to
	// rebuild a player's derived caravan state from their already-purchased upgrades.
	public void applyActions(PlayerData pd) {
		for (CaravanAction action : actions) {
			action.apply(pd);
		}
	}
}
