package me.neoblade298.neorogue.achievement;

import java.util.List;

import me.neoblade298.neorogue.player.unlock.UnlockNode.AchievementRequirement;

/**
 * A configurable command reward. Its commands run when a player gains an achievement tier that
 * exactly matches one of its requirements and every requirement is met at or above its tier.
 * Loaded from achievements.yml.
 */
public class AchievementReward {
	private final String id;
	private final List<AchievementRequirement> requirements;
	private final List<String> commands;

	public AchievementReward(String id, List<AchievementRequirement> requirements, List<String> commands) {
		this.id = id;
		this.requirements = requirements;
		this.commands = commands;
	}

	public String getId() {
		return id;
	}

	public List<AchievementRequirement> getRequirements() {
		return requirements;
	}

	public List<String> getCommands() {
		return commands;
	}
}
