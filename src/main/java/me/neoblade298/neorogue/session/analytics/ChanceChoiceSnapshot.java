package me.neoblade298.neorogue.session.analytics;

import java.util.ArrayList;

// Immutable, Bukkit-free snapshot of a single chance-event pick: the stage a player committed to
// along with every choice that stage offered, each flagged valid (the player met its requirement)
// and picked (the one they chose). Built on the main thread when the choice is clicked, stashed on
// the ChanceInstance, and flushed to AnalyticsManager when the commit actually lands in
// advanceStage. Enables picked/valid pickrate analysis per option.
public class ChanceChoiceSnapshot {
	public final String pickId;
	public final long timestamp;
	public final int balanceVersion;
	public final String playerUuid;
	public final String playerClass;
	public final String host;
	public final int slot;
	public final String runId;
	public final String setId;
	public final String stageId;
	public final String regionType;
	public final String nodeType;
	public final int level;
	public final boolean individual;

	public final ArrayList<ChoiceRow> rows = new ArrayList<ChoiceRow>();

	public ChanceChoiceSnapshot(String pickId, long timestamp, int balanceVersion, String playerUuid, String playerClass,
			String host, int slot, String runId, String setId, String stageId, String regionType, String nodeType, int level,
			boolean individual) {
		this.pickId = pickId;
		this.timestamp = timestamp;
		this.balanceVersion = balanceVersion;
		this.playerUuid = playerUuid;
		this.playerClass = playerClass;
		this.host = host;
		this.slot = slot;
		this.runId = runId;
		this.setId = setId;
		this.stageId = stageId;
		this.regionType = regionType;
		this.nodeType = nodeType;
		this.level = level;
		this.individual = individual;
	}

	// Adds one offered choice to this snapshot.
	public void addChoice(int choiceIndex, String label, boolean valid, boolean picked) {
		rows.add(new ChoiceRow(choiceIndex, label, valid, picked));
	}

	// A single offered choice within a stage.
	public static class ChoiceRow {
		public final int choiceIndex;
		public final String label;
		public final boolean valid;
		public final boolean picked;

		public ChoiceRow(int choiceIndex, String label, boolean valid, boolean picked) {
			this.choiceIndex = choiceIndex;
			this.label = label;
			this.valid = valid;
			this.picked = picked;
		}
	}
}
