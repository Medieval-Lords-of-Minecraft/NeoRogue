package me.neoblade298.neorogue.achievement;

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

	default int getSortPriority() {
		return 0;
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
}
