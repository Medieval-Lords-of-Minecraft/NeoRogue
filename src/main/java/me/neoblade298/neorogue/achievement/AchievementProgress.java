package me.neoblade298.neorogue.achievement;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class AchievementProgress {
	private final Achievement achievement;
	private int progress;

	public AchievementProgress(Achievement achievement, int progress) {
		this.achievement = achievement;
		this.progress = progress;
	}

	public Achievement getAchievement() {
		return achievement;
	}

	public int getProgress() {
		return progress;
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

	public ItemStack toItemStack() {
		int mastery = getMastery();
		int maxMastery = getMaxMastery();
		Material mat = isComplete() ? achievement.getMaterial() : Material.GRAY_DYE;

		ItemStack item = new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(achievement.getDisplayName().decoration(TextDecoration.ITALIC, false));

		List<Component> lore = new ArrayList<>();
		lore.add(Component.text("Mastery: " + mastery + "/" + maxMastery, NamedTextColor.GOLD));
		if (!isComplete()) {
			lore.add(Component.text("Progress: " + progress + "/" + getCurrentThreshold(), NamedTextColor.GRAY));
		} else {
			lore.add(Component.text("Complete!", NamedTextColor.GREEN));
		}
		lore.add(Component.empty());
		lore.addAll(achievement.getDescription(progress, mastery));

		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}
}
