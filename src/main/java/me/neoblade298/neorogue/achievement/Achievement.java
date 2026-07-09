package me.neoblade298.neorogue.achievement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public interface Achievement {
	String getId();
	Component getDisplayName();
	Material getMaterial();
	int[] getMasteryThresholds();
	List<Component> getDescription(int progress, int mastery);
	EnumSet<AchievementTriggerType> getTriggerTypes();

	default AchievementScope getScope() {
		return AchievementScope.BOTH;
	}

	/**
	 * Returns the required class for this achievement, or null if it applies to all classes.
	 * When non-null, this achievement will only appear in that class's tab and only track
	 * progress when playing as that class.
	 */
	default EquipmentClass getRequiredClass() {
		return null;
	}

	default int getSortPriority() {
		return 0;
	}

	/**
	 * Returns the progress display lines for the item tooltip.
	 * Override this for custom progress display (e.g. objective checklists).
	 */
	default List<Component> getProgressLines(AchievementProgress progress) {
		List<Component> lines = new ArrayList<>();
		if (!progress.isComplete()) {
			lines.add(Component.text("Progress: " + progress.getProgress() + "/" + progress.getCurrentThreshold(), NamedTextColor.GRAY));
		} else {
			lines.add(Component.text("Complete!", NamedTextColor.GREEN));
		}
		return lines;
	}

	/**
	 * Returns only the progress summary section used in achievement item lore.
	 */
	default List<Component> getProgressSummaryLines(AchievementProgress progress) {
		return getProgressLines(progress);
	}

	/**
	 * Returns only the objective checklist section used in achievement item lore.
	 */
	default List<Component> getObjectiveLines(AchievementProgress progress) {
		return List.of();
	}

	default void registerSession(Session session, PlayerSessionData data, AchievementProgress progress) {
	}

	default void registerFight(FightInstance fight, PlayerFightData data, AchievementProgress progress) {
	}

	/**
	 * Called when a new mastery tier is reached.
	 * @param p the player
	 * @param mastery the new mastery tier (1-based)
	 * @param ec the class scope (null = global)
	 */
	default void grantReward(Player p, int mastery, EquipmentClass ec) {
	}

	/**
	 * A mastery threshold, in a specific scope, that should be broadcast to the whole server when
	 * reached. {@code classScope} is null for the global scope; {@code tier} is 1-based mastery.
	 */
	public static record AnnouncedThreshold(EquipmentClass classScope, int tier) {
		public static AnnouncedThreshold global(int tier) {
			return new AnnouncedThreshold(null, tier);
		}

		public static AnnouncedThreshold of(EquipmentClass classScope, int tier) {
			return new AnnouncedThreshold(classScope, tier);
		}
	}

	/**
	 * Mastery tiers (per scope) that should be globally announced in chat when earned. Each entry
	 * must match the exact scope (null = global) and tier of the gain. Empty by default.
	 */
	default Collection<AnnouncedThreshold> getAnnouncedThresholds() {
		return List.of();
	}
}
