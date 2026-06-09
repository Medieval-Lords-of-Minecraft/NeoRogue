package me.neoblade298.neorogue.player.unlock;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.neoblade298.neocore.shared.io.Section;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;

public class UnlockNode {
	public static enum TargetType {
		EQUIPMENT, PLAYER_CLASS, OTHER
	}

	private final String id;
	private final TargetType targetType;
	private final EquipmentClass nodeClass; // null = GLOBAL
	private final int cost;
	private final Set<String> targetIds;
	private final Set<String> requirements;

	public UnlockNode(Section sec) {
		this.id = UnlockRegistry.normalizeNodeId(sec.getName());
		this.targetType = TargetType.valueOf(sec.getString("type", "EQUIPMENT").toUpperCase());
		String classStr = sec.getString("class", "GLOBAL").toUpperCase();
		this.nodeClass = classStr.equals("GLOBAL") ? null : EquipmentClass.valueOf(classStr);
		this.cost = sec.getInt("cost", 1);
		List<String> targets = sec.getStringList("targets");
		this.targetIds = targets != null ? Collections.unmodifiableSet(new HashSet<String>(targets)) : Collections.emptySet();
		List<String> reqs = sec.getStringList("requirements");
		this.requirements = reqs != null ? Collections.unmodifiableSet(new HashSet<String>(reqs)) : Collections.emptySet();
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

	public Set<String> getRequirements() {
		return requirements;
	}

	public EquipmentClass getNodeClass() {
		return nodeClass;
	}

	public int getCost() {
		return cost;
	}
}
