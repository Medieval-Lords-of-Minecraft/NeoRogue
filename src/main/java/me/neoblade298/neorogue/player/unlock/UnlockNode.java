package me.neoblade298.neorogue.player.unlock;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class UnlockNode {
	public static enum TargetType {
		EQUIPMENT, PLAYER_CLASS, OTHER
	}

	private final String id;
	private final TargetType targetType;
	private final Set<String> targetIds;
	private final boolean defaultUnlocked;

	public UnlockNode(String id, TargetType targetType, Set<String> targetIds) {
		this(id, targetType, targetIds, false);
	}

	public UnlockNode(String id, TargetType targetType, Set<String> targetIds, boolean defaultUnlocked) {
		this.id = UnlockRegistry.normalizeNodeId(id);
		this.targetType = targetType;
		this.targetIds = Collections.unmodifiableSet(new HashSet<String>(targetIds));
		this.defaultUnlocked = defaultUnlocked;
	}

	public String getId() {
		return id;
	}

	public TargetType getTargetType() {
		return targetType;
	}

	public Set<String> getTargetIds() {
		return targetIds;
	}

	public boolean isDefaultUnlocked() {
		return defaultUnlocked;
	}
}
