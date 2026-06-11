package me.neoblade298.neorogue.achievement;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.Material;

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

	default void registerSession(Session session, PlayerSessionData data, AchievementProgress progress) {
	}

	default void registerFight(FightInstance fight, PlayerFightData data, AchievementProgress progress) {
	}
}
