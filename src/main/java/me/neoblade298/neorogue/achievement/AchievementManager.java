package me.neoblade298.neorogue.achievement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.achievement.builtin.WinFightsAchievement;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

public class AchievementManager {
	private static final List<Achievement> achievements = List.of(
			new WinFightsAchievement()
	);
	private static final HashMap<String, Achievement> achievementsById = new HashMap<>();
	private static final EnumMap<AchievementTriggerType, List<Achievement>> achievementsByTrigger = new EnumMap<>(
			AchievementTriggerType.class
	);
	private static final Map<PlayerFightData, Set<String>> registeredFight = Collections.synchronizedMap(
			new WeakHashMap<>()
	);
	private static final Map<PlayerSessionData, Set<String>> registeredSession = Collections.synchronizedMap(
			new WeakHashMap<>()
	);

	static {
		for (AchievementTriggerType type : AchievementTriggerType.values()) {
			achievementsByTrigger.put(type, new ArrayList<>());
		}

		for (Achievement achievement : achievements) {
			achievementsById.put(achievement.getId(), achievement);
			for (AchievementTriggerType type : achievement.getTriggerTypes()) {
				achievementsByTrigger.get(type).add(achievement);
			}
		}

		for (AchievementTriggerType type : AchievementTriggerType.values()) {
			achievementsByTrigger.put(type, Collections.unmodifiableList(achievementsByTrigger.get(type)));
		}
	}

	private AchievementManager() {
	}

	public static Achievement get(String id) {
		return achievementsById.get(id);
	}

	public static List<Achievement> getAll() {
		return achievements;
	}

	public static void registerSessionAchievements(Session session, PlayerSessionData data) {
		PlayerData pd = data.getData();
		if (pd == null) return;
		for (Achievement achievement : achievementsByTrigger.get(AchievementTriggerType.SESSION)) {
			if (!tryRegister(registeredSession, data, achievement.getId())) continue;
			AchievementProgress progress = pd.getAchievementProgress(achievement.getId());
			if (progress.isComplete()) continue;
			achievement.registerSession(session, data, progress);
		}
	}

	public static void registerFightAchievements(FightInstance fight, PlayerFightData data) {
		PlayerData pd = data.getSessionData().getData();
		if (pd == null) return;
		for (Achievement achievement : achievementsByTrigger.get(AchievementTriggerType.FIGHT)) {
			if (!tryRegister(registeredFight, data, achievement.getId())) continue;
			AchievementProgress progress = pd.getAchievementProgress(achievement.getId());
			if (progress.isComplete()) continue;
			achievement.registerFight(fight, data, progress);
		}
	}

	public static void sendToast(Player p, Achievement achievement, int mastery) {
		Component title = Component.text("Achievement Unlocked!", NamedTextColor.GREEN);
		Component subtitle = achievement.getDisplayName().append(
				Component.text(" (" + mastery + "/" + achievement.getMasteryThresholds().length + ")", NamedTextColor.GOLD)
		);
		p.showTitle(Title.title(title, subtitle));
		p.playSound(p, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1F, 1F);
		p.sendMessage(Component.text("[Achievement] ", NamedTextColor.GREEN).append(subtitle));
	}

	private static <T> boolean tryRegister(Map<T, Set<String>> registrations, T key, String achievementId) {
		synchronized (registrations) {
			Set<String> registered = registrations.get(key);
			if (registered == null) {
				registered = new HashSet<>();
				registrations.put(key, registered);
			}
			return registered.add(achievementId);
		}
	}
}
