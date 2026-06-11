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

	public ItemStack toItemStack() {
		int mastery = getMastery();
		int maxMastery = getMaxMastery();
		Material mat = isComplete() ? achievement.getMaterial() : Material.GRAY_DYE;

		ItemStack item = new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(achievement.getDisplayName().decoration(TextDecoration.ITALIC, false));

		List<Component> lore = new ArrayList<>();
		lore.add(Component.text("Mastery: " + mastery + "/" + maxMastery, NamedTextColor.GOLD));
		lore.addAll(achievement.getProgressLines(this));
		lore.add(Component.empty());
		lore.addAll(achievement.getDescription(progress, mastery));

		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}
}
