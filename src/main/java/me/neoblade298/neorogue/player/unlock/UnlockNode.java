package me.neoblade298.neorogue.player.unlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.shared.io.Section;
import me.neoblade298.neorogue.achievement.AchievementProgress;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class UnlockNode {
	public static enum TargetType {
		EQUIPMENT, PLAYER_CLASS, OTHER
	}

	private final String id;
	private final String displayName;
	private final TargetType targetType;
	private final EquipmentClass nodeClass; // null = GLOBAL
	private final int cost;
	private final Set<String> targetIds;
	private final List<String[]> requirements; // outer = AND, inner = OR (pipe-separated)
	private final Map<String, Integer> achievementRequirements; // achievement ID -> required mastery

	public UnlockNode(Section sec) {
		this.id = UnlockRegistry.normalizeNodeId(sec.getName());
		this.displayName = sec.getString("display-name", null);
		this.targetType = TargetType.valueOf(sec.getString("type", "EQUIPMENT").toUpperCase());
		String classStr = sec.getString("class", "GLOBAL").toUpperCase();
		this.nodeClass = classStr.equals("GLOBAL") ? null : EquipmentClass.valueOf(classStr);
		this.cost = sec.getInt("cost", 1);
		List<String> targets = sec.getStringList("targets");
		this.targetIds = targets != null ? Collections.unmodifiableSet(new HashSet<String>(targets)) : Collections.emptySet();
		List<String> reqs = sec.getStringList("requirements");
		if (reqs != null && !reqs.isEmpty()) {
			ArrayList<String[]> parsed = new ArrayList<>();
			for (String entry : reqs) {
				String[] orGroup = entry.split("\\s*\\|\\s*");
				for (int i = 0; i < orGroup.length; i++) {
					orGroup[i] = UnlockRegistry.normalizeNodeId(orGroup[i].trim());
				}
				parsed.add(orGroup);
			}
			this.requirements = Collections.unmodifiableList(parsed);
		} else {
			this.requirements = Collections.emptyList();
		}
		Section achSec = sec.getSection("achievement-requirements");
		if (achSec != null) {
			HashMap<String, Integer> achReqs = new HashMap<>();
			for (String key : achSec.getKeys()) {
				achReqs.put(key, achSec.getInt(key, 1));
			}
			this.achievementRequirements = Collections.unmodifiableMap(achReqs);
		} else {
			this.achievementRequirements = Collections.emptyMap();
		}
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

	public List<String[]> getRequirements() {
		return requirements;
	}

	public EquipmentClass getNodeClass() {
		return nodeClass;
	}

	public int getCost() {
		return cost;
	}

	public String getDisplayName() {
		return displayName != null ? displayName : prettifyId(id);
	}

	public Map<String, Integer> getAchievementRequirements() {
		return achievementRequirements;
	}

	/**
	 * Creates an ItemStack representing this unlock node for inventory display.
	 * Green = unlocked, Yellow = available (affordable + prereqs met), Red = locked.
	 */
	public ItemStack toItemStack(PlayerData data) {
		boolean unlocked = data.hasUnlockNode(id);
		boolean prereqsMet = checkRequirementsMet(data);
		boolean canAfford = data.getPoints(nodeClass) >= cost;
		boolean available = !unlocked && prereqsMet && canAfford;

		// Material and color by state
		Material mat;
		NamedTextColor color;
		if (unlocked) {
			mat = Material.GREEN_STAINED_GLASS_PANE;
			color = NamedTextColor.GREEN;
		} else if (available) {
			mat = Material.YELLOW_STAINED_GLASS_PANE;
			color = NamedTextColor.YELLOW;
		} else {
			mat = Material.RED_STAINED_GLASS_PANE;
			color = NamedTextColor.RED;
		}

		ItemStack item = new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();

		// Display name
		meta.displayName(Component.text(getDisplayName(), color).decoration(TextDecoration.ITALIC, State.FALSE));

		// Lore
		ArrayList<Component> lore = new ArrayList<Component>();

		// Cost line
		lore.add(Component.text("Cost: ", NamedTextColor.GRAY)
				.append(Component.text(cost, NamedTextColor.YELLOW))
				.append(Component.text(" point" + (cost != 1 ? "s" : ""), NamedTextColor.GRAY))
				.decoration(TextDecoration.ITALIC, State.FALSE));

		// Status line
		if (unlocked) {
			lore.add(Component.text("\u2714 Unlocked", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, State.FALSE));
		} else if (available) {
			lore.add(Component.text("Available", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, State.FALSE));
		} else if (!prereqsMet) {
			lore.add(Component.text("Locked (missing prerequisites)", NamedTextColor.RED).decoration(TextDecoration.ITALIC, State.FALSE));
		} else {
			lore.add(Component.text("Locked (not enough points)", NamedTextColor.RED).decoration(TextDecoration.ITALIC, State.FALSE));
		}

		// Separator
		lore.add(Component.empty());

		// Targets
		lore.add(Component.text("Unlocks:", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, State.FALSE));
		for (String targetId : targetIds) {
			Component targetDisplay;
			if (targetType == TargetType.EQUIPMENT) {
				Equipment eq = Equipment.get(targetId, false);
				if (eq != null) {
					targetDisplay = Component.text(" - ", NamedTextColor.GRAY).append(eq.getDisplay());
				} else {
					targetDisplay = Component.text(" - " + targetId, NamedTextColor.GRAY);
				}
			} else if (targetType == TargetType.PLAYER_CLASS) {
				targetDisplay = Component.text(" - " + targetId, NamedTextColor.AQUA);
			} else {
				targetDisplay = Component.text(" - " + targetId, NamedTextColor.GRAY);
			}
			lore.add(targetDisplay.decoration(TextDecoration.ITALIC, State.FALSE));
		}

		// Requirements
		if (!requirements.isEmpty() || !achievementRequirements.isEmpty()) {
			lore.add(Component.empty());
			lore.add(Component.text("Requires:", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, State.FALSE));
			for (String[] orGroup : requirements) {
				if (orGroup.length == 1) {
					NamedTextColor reqColor = data.hasUnlockNode(orGroup[0]) ? NamedTextColor.GREEN : NamedTextColor.RED;
					UnlockNode reqNode = UnlockRegistry.getNode(orGroup[0]);
					String reqName = reqNode != null ? reqNode.getDisplayName() : prettifyId(orGroup[0]);
					lore.add(Component.text(" - " + reqName, reqColor).decoration(TextDecoration.ITALIC, State.FALSE));
				} else {
					// OR group: check if any is met
					boolean anyMet = false;
					StringBuilder names = new StringBuilder();
					for (int i = 0; i < orGroup.length; i++) {
						if (data.hasUnlockNode(orGroup[i])) anyMet = true;
						UnlockNode reqNode = UnlockRegistry.getNode(orGroup[i]);
						if (i > 0) names.append(" or ");
						names.append(reqNode != null ? reqNode.getDisplayName() : prettifyId(orGroup[i]));
					}
					NamedTextColor reqColor = anyMet ? NamedTextColor.GREEN : NamedTextColor.RED;
					lore.add(Component.text(" - " + names, reqColor).decoration(TextDecoration.ITALIC, State.FALSE));
				}
			}
			for (Map.Entry<String, Integer> entry : achievementRequirements.entrySet()) {
				AchievementProgress progress = data.getAchievementProgress(entry.getKey());
				boolean met = progress != null && progress.getMastery() >= entry.getValue();
				String achName = progress != null ? progress.getAchievement().getId() : entry.getKey();
				NamedTextColor achColor = met ? NamedTextColor.GREEN : NamedTextColor.RED;
				lore.add(Component.text(" - " + prettifyId(achName) + " (Mastery " + entry.getValue() + ")", achColor)
						.decoration(TextDecoration.ITALIC, State.FALSE));
			}
		}

		meta.lore(lore);
		item.setItemMeta(meta);

		// Add NBT tag for click handling
		NBTItem nbti = new NBTItem(item);
		nbti.setString("unlockNodeId", id);
		return nbti.getItem();
	}

	/**
	 * Checks if all requirements (node AND/OR groups + achievement requirements) are met.
	 */
	public boolean checkRequirementsMet(PlayerData data) {
		for (String[] orGroup : requirements) {
			boolean anyMet = false;
			for (String req : orGroup) {
				if (data.hasUnlockNode(req)) {
					anyMet = true;
					break;
				}
			}
			if (!anyMet) return false;
		}
		for (Map.Entry<String, Integer> entry : achievementRequirements.entrySet()) {
			AchievementProgress progress = data.getAchievementProgress(entry.getKey());
			if (progress == null || progress.getMastery() < entry.getValue()) return false;
		}
		return true;
	}

	private static String prettifyId(String id) {
		String[] parts = id.split("_");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			if (i > 0) sb.append(' ');
			if (parts[i].length() > 0) {
				sb.append(Character.toUpperCase(parts[i].charAt(0)));
				if (parts[i].length() > 1) sb.append(parts[i].substring(1));
			}
		}
		return sb.toString();
	}
}
