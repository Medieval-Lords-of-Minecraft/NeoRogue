package me.neoblade298.neorogue.achievement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Abstract achievement type for tracking completion of individual objectives.
 * Progress is stored as the count of completed objectives.
 * Data stores the comma-separated IDs of completed objectives.
 */
public abstract class ObjectiveAchievement implements Achievement {

	/**
	 * Returns all possible objective IDs for this achievement.
	 */
	public abstract List<String> getObjectiveIds();

	/**
	 * Returns the human-readable display name for a given objective ID.
	 */
	public abstract String getObjectiveDisplay(String id);

	@Override
	public int[] getMasteryThresholds() {
		return new int[] { getObjectiveIds().size() };
	}

	@Override
	public List<Component> getProgressLines(AchievementProgress progress) {
		List<Component> lines = new ArrayList<>();
		lines.addAll(getProgressSummaryLines(progress));
		lines.add(Component.empty());
		lines.addAll(getObjectiveLines(progress));
		return lines;
	}

	@Override
	public List<Component> getProgressSummaryLines(AchievementProgress progress) {
		Set<String> completed = parseData(progress.getData());
		List<Component> lines = new ArrayList<>();
		if (progress.isComplete()) {
			lines.add(Component.text("Complete!", NamedTextColor.GREEN));
		} else {
			lines.add(Component.text("Progress: " + completed.size() + "/" + getObjectiveIds().size(), NamedTextColor.GRAY));
		}
		return lines;
	}

	@Override
	public List<Component> getObjectiveLines(AchievementProgress progress) {
		Set<String> completed = parseData(progress.getData());
		List<String> sortedIds = new ArrayList<>(getObjectiveIds());
		sortedIds.sort((a, b) -> getObjectiveDisplay(a).compareToIgnoreCase(getObjectiveDisplay(b)));
		List<Component> lines = new ArrayList<>();
		for (String objId : sortedIds) {
			boolean done = completed.contains(objId);
			String name = getObjectiveDisplay(objId);
			lines.add(Component.text((done ? "\u2714" : "\u2718") + " " + name,
					done ? NamedTextColor.GREEN : NamedTextColor.GRAY));
		}
		return lines;
	}

	/**
	 * Marks an objective as complete. Returns true if a new mastery tier was reached.
	 */
	public boolean completeObjective(AchievementProgress progress, String objectiveId) {
		Set<String> completed = parseData(progress.getData());
		if (!completed.add(objectiveId)) return false; // already done
		progress.setData(String.join(",", completed));
		return progress.addProgress(1);
	}

	/**
	 * Checks if a specific objective has been completed.
	 */
	public boolean isObjectiveComplete(AchievementProgress progress, String objectiveId) {
		return parseData(progress.getData()).contains(objectiveId);
	}

	private Set<String> parseData(String data) {
		Set<String> set = new HashSet<>();
		if (data == null || data.isBlank()) return set;
		for (String s : data.split(",")) {
			set.add(s);
		}
		return set;
	}
}
