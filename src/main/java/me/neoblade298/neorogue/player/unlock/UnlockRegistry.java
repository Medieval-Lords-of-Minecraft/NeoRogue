package me.neoblade298.neorogue.player.unlock;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.shared.io.Section;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.DropTableSet;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentType;
import me.neoblade298.neorogue.player.PlayerData;

public class UnlockRegistry {
	private static final LinkedHashMap<String, UnlockNode> nodes = new LinkedHashMap<String, UnlockNode>();
	private static final HashMap<String, HashSet<String>> equipmentToNodes = new HashMap<String, HashSet<String>>();
	private static final HashMap<EquipmentClass, HashSet<String>> classToNodes = new HashMap<EquipmentClass, HashSet<String>>();
	private static final HashMap<String, HashSet<String>> nodeEquipmentTargets = new HashMap<String, HashSet<String>>();
	private static final HashSet<String> defaultNodes = new HashSet<String>();

	static {
		reload();
	}

	private UnlockRegistry() {
	}

	public static synchronized void reload() {
		nodes.clear();
		equipmentToNodes.clear();
		classToNodes.clear();
		nodeEquipmentTargets.clear();
		defaultNodes.clear();
		loadNodes();

		for (UnlockNode node : nodes.values()) {
			switch (node.getTargetType()) {
			case EQUIPMENT:
				HashSet<String> equipmentIds = new HashSet<String>();
				for (String targetId : node.getTargetIds()) {
					String normalizedId = targetId.toLowerCase(Locale.ROOT);
					equipmentIds.add(normalizedId);
					equipmentToNodes.computeIfAbsent(normalizedId, key -> new HashSet<String>()).add(node.getId());
					Equipment eq = Equipment.get(targetId, false);
					if (eq == null) {
						Bukkit.getLogger().warning("[NeoRogue] Unlock node " + node.getId()
								+ " references unknown equipment id " + targetId);
						continue;
					}
					if (eq.getType() == EquipmentType.ARTIFACT) {
						Equipment.removeFromArtifactDroptable((Artifact) eq);
					} else if (eq.getType() == EquipmentType.CONSUMABLE) {
						Equipment.removeFromConsumablesDroptable((Consumable) eq);
					} else {
						Equipment.removeFromDroptable(eq);
					}
				}
				nodeEquipmentTargets.put(node.getId(), equipmentIds);
				break;
			case PLAYER_CLASS:
				for (String targetId : node.getTargetIds()) {
					try {
						EquipmentClass ec = EquipmentClass.valueOf(targetId.toUpperCase(Locale.ROOT));
						classToNodes.computeIfAbsent(ec, key -> new HashSet<String>()).add(node.getId());
					}
					catch (IllegalArgumentException e) {
						Bukkit.getLogger().warning("[NeoRogue] Unlock node " + node.getId()
								+ " references unknown class " + targetId);
					}
				}
				break;
			case OTHER:
			default:
				break;
			}
		}
	}

	private static void loadNodes() {
		NeoCore.loadFiles(new File(NeoRogue.inst().getDataFolder(), "unlocks"), (yml, file) -> {
			for (String key : yml.getKeys()) {
				try {
					Section sec = yml.getSection(key);
					UnlockNode node = new UnlockNode(sec);
					register(node);
				} catch (Exception e) {
					e.printStackTrace();
					Bukkit.getLogger().warning("[NeoRogue] Failed to load unlock node " + key + " in file " + file.getName());
				}
			}
		});
	}

	public static synchronized void register(UnlockNode node) {
		nodes.put(node.getId(), node);
		if (node.isDefault()) {
			defaultNodes.add(node.getId());
		}
	}

	public static Set<String> getDefaultNodes() {
		return Collections.unmodifiableSet(defaultNodes);
	}

	public static String normalizeNodeId(String nodeId) {
		return nodeId == null ? "" : nodeId.toLowerCase(Locale.ROOT);
	}

	public static boolean hasNode(String nodeId) {
		return nodes.containsKey(normalizeNodeId(nodeId));
	}

	public static Set<String> getNodeIds() {
		return Collections.unmodifiableSet(nodes.keySet());
	}

	public static boolean isEquipmentUnlockedFor(PlayerData data, String equipmentId) {
		if (equipmentId == null) return false;
		HashSet<String> requiredNodes = equipmentToNodes.get(equipmentId.toLowerCase(Locale.ROOT));
		if (requiredNodes == null || requiredNodes.isEmpty()) return true;
		if (data == null) return false;
		for (String nodeId : requiredNodes) {
			if (data.hasUnlockNode(nodeId)) return true;
		}
		return false;
	}

	public static boolean isClassUnlockedFor(PlayerData data, EquipmentClass equipmentClass) {
		if (equipmentClass == EquipmentClass.CLASSLESS || equipmentClass == EquipmentClass.SHOP) return true;
		HashSet<String> requiredNodes = classToNodes.get(equipmentClass);
		if (requiredNodes == null || requiredNodes.isEmpty()) return true;
		if (data == null) return false;
		for (String nodeId : requiredNodes) {
			if (data.hasUnlockNode(nodeId)) return true;
		}
		return false;
	}

	public static DropTableSet<Equipment> buildEquipmentDroptable(PlayerData data) {
		DropTableSet<Equipment> derived = Equipment.copyDropSet();
		if (data != null) {
			for (String unlockNode : data.getUnlockNodes()) {
				HashSet<String> nodeTargets = nodeEquipmentTargets.get(normalizeNodeId(unlockNode));
				if (nodeTargets == null || nodeTargets.isEmpty()) continue;
				for (String equipmentId : nodeTargets) {
					Equipment eq = Equipment.get(equipmentId, false);
					if (eq == null) continue;
					if (eq.getType() == EquipmentType.ARTIFACT || eq.getType() == EquipmentType.CONSUMABLE) continue;
					derived.add(eq.getEquipmentClasses(), eq);
				}
			}
		}

		if (derived.isEmpty()) {
			Bukkit.getLogger().warning("[NeoRogue] Derived unlock droptable was empty; using base droptable");
			return Equipment.copyDropSet();
		}
		return derived;
	}

	public static DropTableSet<Artifact> buildArtifactDroptable(PlayerData data) {
		DropTableSet<Artifact> derived = Equipment.copyArtifactsDropSet();
		if (data != null) {
			for (String unlockNode : data.getUnlockNodes()) {
				HashSet<String> nodeTargets = nodeEquipmentTargets.get(normalizeNodeId(unlockNode));
				if (nodeTargets == null || nodeTargets.isEmpty()) continue;
				for (String equipmentId : nodeTargets) {
					Equipment eq = Equipment.get(equipmentId, false);
					if (eq == null || eq.getType() != EquipmentType.ARTIFACT) continue;
					derived.add(eq.getEquipmentClasses(), (Artifact) eq);
				}
			}
		}
		return derived;
	}

	public static DropTableSet<Consumable> buildConsumableDroptable(PlayerData data) {
		DropTableSet<Consumable> derived = Equipment.copyConsumablesDropSet();
		if (data != null) {
			for (String unlockNode : data.getUnlockNodes()) {
				HashSet<String> nodeTargets = nodeEquipmentTargets.get(normalizeNodeId(unlockNode));
				if (nodeTargets == null || nodeTargets.isEmpty()) continue;
				for (String equipmentId : nodeTargets) {
					Equipment eq = Equipment.get(equipmentId, false);
					if (eq == null || eq.getType() != EquipmentType.CONSUMABLE) continue;
					derived.add(eq.getEquipmentClasses(), (Consumable) eq);
				}
			}
		}
		return derived;
	}

	public static Map<String, UnlockNode> getNodes() {
		return Collections.unmodifiableMap(nodes);
	}

	public static ArrayList<String> getSortedNodeIds() {
		ArrayList<String> ids = new ArrayList<String>(nodes.keySet());
		Collections.sort(ids);
		return ids;
	}

	public static UnlockNode getNode(String nodeId) {
		return nodes.get(normalizeNodeId(nodeId));
	}

	public static ArrayList<UnlockNode> getNodesForClass(EquipmentClass ec) {
		ArrayList<UnlockNode> result = new ArrayList<UnlockNode>();
		for (UnlockNode node : nodes.values()) {
			if (node.getNodeClass() == ec) {
				result.add(node);
			}
		}
		return result;
	}

	public static boolean canAfford(PlayerData data, UnlockNode node) {
		if (data == null || node == null) return false;
		return data.getPoints(node.getNodeClass()) >= node.getCost();
	}

	/**
	 * Attempts to grant a node to a player, deducting points.
	 * Returns true if successful, false if cannot afford or already unlocked.
	 */
	public static boolean grantWithCost(PlayerData data, String nodeId) {
		String normalized = normalizeNodeId(nodeId);
		UnlockNode node = nodes.get(normalized);
		if (node == null) return false;
		if (data.hasUnlockNode(normalized)) return false;
		if (!canAfford(data, node)) return false;
		if (!node.checkRequirementsMet(data)) return false;
		data.addPoints(node.getNodeClass(), -node.getCost());
		return data.grant(normalized);
	}
}
