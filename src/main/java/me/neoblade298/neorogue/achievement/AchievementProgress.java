package me.neoblade298.neorogue.achievement;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class AchievementProgress {
	private final Achievement achievement;
	private final EquipmentClass scope; // null = global
	private int progress;
	private String data;

	public AchievementProgress(Achievement achievement, int progress) {
		this(achievement, progress, null, null);
	}

	public AchievementProgress(Achievement achievement, int progress, EquipmentClass scope) {
		this(achievement, progress, scope, null);
	}

	public AchievementProgress(Achievement achievement, int progress, EquipmentClass scope, String data) {
		this.achievement = achievement;
		this.progress = progress;
		this.scope = scope;
		this.data = data;
	}

	public Achievement getAchievement() {
		return achievement;
	}

	/**
	 * Returns the scope of this progress instance.
	 * Null means global, non-null means class-specific.
	 */
	public EquipmentClass getScope() {
		return scope;
	}

	public int getProgress() {
		return progress;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public int getMastery() {
		int[] thresholds = achievement.getMasteryThresholds();
		int mastery = 0;
		for (int threshold : thresholds) {
			if (progress >= threshold) {
				mastery++;
			} else {
				break;
			}
		}
		return mastery;
	}

	public int getMaxMastery() {
		return achievement.getMasteryThresholds().length;
	}

	public boolean isComplete() {
		return getMastery() >= getMaxMastery();
	}

	public int getCurrentThreshold() {
		int mastery = getMastery();
		int[] thresholds = achievement.getMasteryThresholds();
		if (mastery >= thresholds.length) return thresholds[thresholds.length - 1];
		return thresholds[mastery];
	}

	/**
	 * Adds progress and returns true if a new mastery tier was reached.
	 */
	public boolean addProgress(int amount) {
		int oldMastery = getMastery();
		progress += amount;
		return getMastery() > oldMastery;
	}

	public List<Component> buildLoreLines() {
		int mastery = getMastery();
		int maxMastery = getMaxMastery();
		List<Component> lines = new ArrayList<>();
		lines.add(Component.text("Mastery: " + mastery + "/" + maxMastery, NamedTextColor.GOLD));
		lines.addAll(achievement.getProgressSummaryLines(this));
		lines.add(Component.empty());
		lines.addAll(achievement.getDescription(progress, mastery));
		List<Component> objectiveLines = achievement.getObjectiveLines(this);
		if (!objectiveLines.isEmpty()) {
			lines.add(Component.empty());
			lines.addAll(objectiveLines);
		}
		return lines;
	}

	private static final Material[] MASTERY_DYES = {
		Material.GRAY_DYE,       // mastery 0 — no progress
		Material.YELLOW_DYE,     // mastery 1
		Material.LIME_DYE,       // mastery 2
		Material.GREEN_DYE,      // mastery 3
		Material.CYAN_DYE,       // mastery 4+
	};

	public ItemStack toItemStack() {
		Material mat = isComplete()
				? achievement.getMaterial()
				: MASTERY_DYES[Math.min(getMastery(), MASTERY_DYES.length - 1)];

		ItemStack item = new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(achievement.getDisplayName().decoration(TextDecoration.ITALIC, false));

		List<Component> lore = buildLoreLines();
		lore.replaceAll(line -> line.decoration(TextDecoration.ITALIC, false));
		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}
}
