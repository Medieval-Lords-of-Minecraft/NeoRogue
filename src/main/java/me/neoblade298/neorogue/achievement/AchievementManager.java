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

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.achievement.builtin.AllBossesAchievement;
import me.neoblade298.neorogue.achievement.builtin.AllMinibossesAchievement;
import me.neoblade298.neorogue.achievement.builtin.BeatMinibossesAchievement;
import me.neoblade298.neorogue.achievement.builtin.BeatRegionAchievement;
import me.neoblade298.neorogue.achievement.builtin.FinishRunAchievement;
import me.neoblade298.neorogue.achievement.builtin.FlawlessRegionAchievement;
import me.neoblade298.neorogue.achievement.builtin.FullPartyAchievement;
import me.neoblade298.neorogue.achievement.builtin.NoHealthLossAchievement;
import me.neoblade298.neorogue.achievement.builtin.WinFightsAchievement;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

public class AchievementManager {
	private static final List<Achievement> achievements = List.of(
			new WinFightsAchievement(),
			new FinishRunAchievement(),
			new BeatMinibossesAchievement(),
			new BeatRegionAchievement("beat_ld", Component.text("Low District Victor", NamedTextColor.GOLD),
					Material.COBBLESTONE, RegionType.LOW_DISTRICT),
			new BeatRegionAchievement("beat_hf", Component.text("Harvest Fields Victor", NamedTextColor.GOLD),
					Material.HAY_BLOCK, RegionType.HARVEST_FIELDS),
			new BeatRegionAchievement("beat_fw", Component.text("Frozen Wastes Victor", NamedTextColor.GOLD),
					Material.PACKED_ICE, RegionType.FROZEN_WASTES),
			new FullPartyAchievement(),
			new AllMinibossesAchievement("all_minis_ld", Component.text("LD Mini Slayer", NamedTextColor.GOLD),
					Material.STONE_SWORD, RegionType.LOW_DISTRICT),
			new AllMinibossesAchievement("all_minis_hf", Component.text("HF Mini Slayer", NamedTextColor.GOLD),
					Material.STONE_SWORD, RegionType.HARVEST_FIELDS),
			new AllMinibossesAchievement("all_minis_fw", Component.text("FW Mini Slayer", NamedTextColor.GOLD),
					Material.STONE_SWORD, RegionType.FROZEN_WASTES),
			new AllBossesAchievement("all_bosses_ld", Component.text("LD Boss Slayer", NamedTextColor.GOLD),
					Material.DIAMOND_SWORD, RegionType.LOW_DISTRICT),
			new AllBossesAchievement("all_bosses_hf", Component.text("HF Boss Slayer", NamedTextColor.GOLD),
					Material.DIAMOND_SWORD, RegionType.HARVEST_FIELDS),
			new AllBossesAchievement("all_bosses_fw", Component.text("FW Boss Slayer", NamedTextColor.GOLD),
					Material.DIAMOND_SWORD, RegionType.FROZEN_WASTES),
			new NoHealthLossAchievement(),
			new FlawlessRegionAchievement()
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

	/**
	 * Returns the list of achievements visible in the given scope.
	 * If ec is null, returns achievements with scope GLOBAL or BOTH.
	 * If ec is non-null, returns achievements with scope CLASS or BOTH.
	 */
	public static List<Achievement> getForScope(EquipmentClass ec) {
		List<Achievement> result = new ArrayList<>();
		for (Achievement ach : achievements) {
			AchievementScope scope = ach.getScope();
			if (ec == null) {
				if (scope == AchievementScope.GLOBAL || scope == AchievementScope.BOTH) {
					result.add(ach);
				}
			} else {
				if (scope == AchievementScope.CLASS || scope == AchievementScope.BOTH) {
					result.add(ach);
				}
			}
		}
		return result;
	}

	public static void registerSessionAchievements(Session session, PlayerSessionData data) {
		PlayerData pd = data.getData();
		if (pd == null) return;
		EquipmentClass ec = data.getPlayerClass();
		for (Achievement achievement : achievementsByTrigger.get(AchievementTriggerType.SESSION)) {
			AchievementScope scope = achievement.getScope();
			// Register global progress
			if (scope == AchievementScope.GLOBAL || scope == AchievementScope.BOTH) {
				String regKey = achievement.getId() + ":GLOBAL";
				if (tryRegister(registeredSession, data, regKey)) {
					AchievementProgress progress = pd.getGlobalAchievementProgress(achievement.getId());
					if (!progress.isComplete()) {
						achievement.registerSession(session, data, progress);
					}
				}
			}
			// Register class progress
			if ((scope == AchievementScope.CLASS || scope == AchievementScope.BOTH) && ec != null) {
				String regKey = achievement.getId() + ":" + ec.name();
				if (tryRegister(registeredSession, data, regKey)) {
					AchievementProgress progress = pd.getClassAchievementProgress(achievement.getId(), ec);
					if (!progress.isComplete()) {
						achievement.registerSession(session, data, progress);
					}
				}
			}
		}
	}

	public static void registerFightAchievements(FightInstance fight, PlayerFightData data) {
		PlayerData pd = data.getSessionData().getData();
		if (pd == null) return;
		EquipmentClass ec = data.getSessionData().getPlayerClass();
		for (Achievement achievement : achievementsByTrigger.get(AchievementTriggerType.FIGHT)) {
			AchievementScope scope = achievement.getScope();
			// Register global progress
			if (scope == AchievementScope.GLOBAL || scope == AchievementScope.BOTH) {
				String regKey = achievement.getId() + ":GLOBAL";
				if (tryRegister(registeredFight, data, regKey)) {
					AchievementProgress progress = pd.getGlobalAchievementProgress(achievement.getId());
					if (!progress.isComplete()) {
						achievement.registerFight(fight, data, progress);
					}
				}
			}
			// Register class progress
			if ((scope == AchievementScope.CLASS || scope == AchievementScope.BOTH) && ec != null) {
				String regKey = achievement.getId() + ":" + ec.name();
				if (tryRegister(registeredFight, data, regKey)) {
					AchievementProgress progress = pd.getClassAchievementProgress(achievement.getId(), ec);
					if (!progress.isComplete()) {
						achievement.registerFight(fight, data, progress);
					}
				}
			}
		}
	}

	/**
	 * Call this when progress.addProgress() returns true (new mastery reached).
	 * Sends the toast and calls grantReward on the achievement.
	 */
	public static void notifyMastery(Player p, Achievement achievement, AchievementProgress progress) {
		int mastery = progress.getMastery();
		EquipmentClass ec = progress.getScope();
		sendToast(p, achievement, mastery, ec);
		achievement.grantReward(p, mastery, ec);
	}

	public static void sendToast(Player p, Achievement achievement, int mastery, EquipmentClass ec) {
		Component title = Component.text("Achievement Unlocked!", NamedTextColor.GREEN);
		Component subtitle = achievement.getDisplayName().append(
				Component.text(" (" + mastery + "/" + achievement.getMasteryThresholds().length + ")", NamedTextColor.GOLD)
		);
		if (ec != null) {
			subtitle = subtitle.append(Component.text(" [" + ec.getDisplay() + "]", NamedTextColor.YELLOW));
		}
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
