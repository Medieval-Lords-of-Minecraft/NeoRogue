package me.neoblade298.neorogue.achievement;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.achievement.builtin.AcquireRarityAchievement;
import me.neoblade298.neorogue.achievement.builtin.AllBossesAchievement;
import me.neoblade298.neorogue.achievement.builtin.AllMinibossesAchievement;
import me.neoblade298.neorogue.achievement.builtin.BeatMinibossesAchievement;
import me.neoblade298.neorogue.achievement.builtin.BeatRegionAchievement;
import me.neoblade298.neorogue.achievement.builtin.FinishRunAchievement;
import me.neoblade298.neorogue.achievement.builtin.FlawlessRegionAchievement;
import me.neoblade298.neorogue.achievement.builtin.FullPartyAchievement;
import me.neoblade298.neorogue.achievement.builtin.MaxStatAchievement;
import me.neoblade298.neorogue.achievement.builtin.MaxStatAchievement.StatType;
import me.neoblade298.neorogue.achievement.builtin.NoHealthLossAchievement;
import me.neoblade298.neorogue.achievement.builtin.SRankRegionAchievement;
import me.neoblade298.neorogue.achievement.builtin.SpendCoinsAchievement;
import me.neoblade298.neorogue.achievement.builtin.VisitNodesAchievement;
import me.neoblade298.neorogue.achievement.builtin.WinFightsAchievement;
import me.neoblade298.neorogue.achievement.builtin.WinRunsAchievement;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class AchievementManager {
	private static final List<Achievement> achievements = List.of(
			new WinFightsAchievement(),
			new WinRunsAchievement(),
			new FinishRunAchievement(),
			new BeatMinibossesAchievement(),
new BeatRegionAchievement("low_district_victor", Component.text("Low District Victor", NamedTextColor.GOLD),
				Material.COBBLESTONE, RegionType.LOW_DISTRICT),
			new BeatRegionAchievement("harvest_fields_victor", Component.text("Harvest Fields Victor", NamedTextColor.GOLD),
				Material.HAY_BLOCK, RegionType.HARVEST_FIELDS),
			new BeatRegionAchievement("frozen_wastes_victor", Component.text("Frozen Wastes Victor", NamedTextColor.GOLD),
					Material.PACKED_ICE, RegionType.FROZEN_WASTES),
			new FullPartyAchievement(),
new AllMinibossesAchievement("low_district_miniboss_slayer", Component.text("Low District Miniboss Slayer", NamedTextColor.GOLD),
				Material.STONE_SWORD, RegionType.LOW_DISTRICT),
			new AllMinibossesAchievement("harvest_fields_miniboss_slayer", Component.text("Harvest Fields Miniboss Slayer", NamedTextColor.GOLD),
				Material.STONE_SWORD, RegionType.HARVEST_FIELDS),
			new AllMinibossesAchievement("frozen_wastes_miniboss_slayer", Component.text("Frozen Wastes Miniboss Slayer", NamedTextColor.GOLD),
					Material.STONE_SWORD, RegionType.FROZEN_WASTES),
new AllBossesAchievement("low_district_boss_slayer", Component.text("Low District Boss Slayer", NamedTextColor.GOLD),
				Material.DIAMOND_SWORD, RegionType.LOW_DISTRICT),
			new AllBossesAchievement("harvest_fields_boss_slayer", Component.text("Harvest Fields Boss Slayer", NamedTextColor.GOLD),
				Material.DIAMOND_SWORD, RegionType.HARVEST_FIELDS),
			new AllBossesAchievement("frozen_wastes_boss_slayer", Component.text("Frozen Wastes Boss Slayer", NamedTextColor.GOLD),
					Material.DIAMOND_SWORD, RegionType.FROZEN_WASTES),
			new NoHealthLossAchievement(),
			new FlawlessRegionAchievement(),
new MaxStatAchievement("beefy", Component.text("Beefy", NamedTextColor.GOLD),
				Material.GOLDEN_APPLE, StatType.HEALTH, 200),
			new MaxStatAchievement("arcane_reservoir", Component.text("Arcane Reservoir", NamedTextColor.GOLD),
				Material.LAPIS_LAZULI, StatType.MANA, 100),
			new MaxStatAchievement("tireless", Component.text("Tireless", NamedTextColor.GOLD),
					Material.FEATHER, StatType.STAMINA, 100),
new AcquireRarityAchievement("rare_find", Component.text("Rare Find", NamedTextColor.GOLD),
				Material.GOLD_INGOT, Rarity.RARE),
			new AcquireRarityAchievement("epic_discovery", Component.text("Epic Discovery", NamedTextColor.GOLD),
					Material.DIAMOND, Rarity.EPIC),
			new VisitNodesAchievement(),
			new SpendCoinsAchievement(),
new SRankRegionAchievement("low_district_speedster", Component.text("Low District Speedster", NamedTextColor.GOLD),
				Material.CLOCK, RegionType.LOW_DISTRICT),
			new SRankRegionAchievement("harvest_fields_speedster", Component.text("Harvest Fields Speedster", NamedTextColor.GOLD),
				Material.CLOCK, RegionType.HARVEST_FIELDS),
			new SRankRegionAchievement("frozen_wastes_speedster", Component.text("Frozen Wastes Speedster", NamedTextColor.GOLD),
					Material.CLOCK, RegionType.FROZEN_WASTES)
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
					EquipmentClass req = ach.getRequiredClass();
					if (req != null && req != ec) continue;
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
				EquipmentClass req = achievement.getRequiredClass();
				if (req != null && req != ec) continue;
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
				EquipmentClass req = achievement.getRequiredClass();
				if (req != null && req != ec) continue;
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
		sendToast(p, achievement, mastery, ec, progress);
		achievement.grantReward(p, mastery, ec);
		AchievementRewardRegistry.handleAchievementGained(p, achievement, progress);

		// Globally announce this exact (scope, tier) if the achievement marked it. The enum ==
		// comparison handles null == null for the global scope, so scope matches exactly.
		for (Achievement.AnnouncedThreshold at : achievement.getAnnouncedThresholds()) {
			if (at.classScope() == ec && at.tier() == mastery) {
				broadcastAnnouncement(p, achievement, mastery, ec, progress);
				break;
			}
		}
	}

	// Broadcasts a globally-announced threshold to every online player except the earner, who
	// already receives their personal toast and chat message.
	private static void broadcastAnnouncement(Player earner, Achievement achievement, int mastery, EquipmentClass ec,
			AchievementProgress progress) {
		Component hoverText = buildAchievementHover(achievement, mastery, ec, progress);
		String scope = ec != null ? ec.name().toLowerCase() : "global";
		Component msg = Component.text("[Achievement] ", NamedTextColor.YELLOW)
				.append(Component.text(earner.getName(), NamedTextColor.GOLD))
				.append(Component.text(" earned ", NamedTextColor.GRAY))
				.append(achievement.getDisplayName())
				.append(Component.text(" (" + mastery + "/" + achievement.getMasteryThresholds().length + ")", NamedTextColor.GOLD));
		if (ec != null) {
			msg = msg.append(Component.text(" [" + ec.getDisplay() + "]", NamedTextColor.YELLOW));
		}
		else {
			msg = msg.append(Component.text(" [Global]", NamedTextColor.GRAY));
		}
		msg = msg.append(Component.text("!", NamedTextColor.GRAY))
				.hoverEvent(HoverEvent.showText(hoverText))
				.clickEvent(ClickEvent.runCommand("/nr achievements " + scope));
		for (Player online : Bukkit.getOnlinePlayers()) {
			if (online.equals(earner)) continue;
			online.sendMessage(msg);
		}
	}

	private static class ToastEntry {
		final String title, description;
		final Material icon;
		ToastEntry(String title, String description, Material icon) {
			this.title = title; this.description = description; this.icon = icon;
		}
	}

	private static final HashMap<UUID, Deque<ToastEntry>> toastQueues = new HashMap<>();
	private static final long TOAST_DURATION_TICKS = 100L; // ~5 seconds, matches client toast display time

	public static void sendToast(Player p, Achievement achievement, int mastery, EquipmentClass ec) {
		sendToast(p, achievement, mastery, ec, null);
	}

	public static void sendToast(Player p, Achievement achievement, int mastery, EquipmentClass ec, AchievementProgress progress) {
		String displayName = PlainTextComponentSerializer.plainText().serialize(achievement.getDisplayName());
		if (ec != null) {
			displayName += " [" + ec.getDisplay() + "]";
		}
		else {
			displayName += " [Global]";
		}
		String description = "Mastery " + mastery + "/" + achievement.getMasteryThresholds().length;
		UUID uuid = p.getUniqueId();
		Deque<ToastEntry> queue = toastQueues.computeIfAbsent(uuid, k -> new ArrayDeque<>());
		queue.addLast(new ToastEntry(displayName, description, achievement.getMaterial()));
		processToastQueue(uuid);

		// Build hover text using the same lore structure as the inventory item
		Component hoverText = buildAchievementHover(achievement, mastery, ec, progress);

		String scope = ec != null ? ec.name().toLowerCase() : "global";
		Component chatMsg = Component.text("[Achievement] ", NamedTextColor.YELLOW)
				.append(achievement.getDisplayName())
				.append(Component.text(" (" + mastery + "/" + achievement.getMasteryThresholds().length + ")", NamedTextColor.GOLD));
		if (ec != null) {
			chatMsg = chatMsg.append(Component.text(" [" + ec.getDisplay() + "]", NamedTextColor.YELLOW));
		}
		else {
			chatMsg = chatMsg.append(Component.text(" [Global]", NamedTextColor.GRAY));
		}
		chatMsg = chatMsg.hoverEvent(HoverEvent.showText(hoverText))
				.clickEvent(ClickEvent.runCommand("/nr achievements " + scope));
		p.sendMessage(chatMsg);
	}

	// Builds the shared hover tooltip (class line + lore + "click to view") used by both the
	// personal achievement message and the global announcement.
	private static Component buildAchievementHover(Achievement achievement, int mastery, EquipmentClass ec,
			AchievementProgress progress) {
		Component hoverText = achievement.getDisplayName();
		if (ec != null) {
			hoverText = hoverText.append(Component.newline())
					.append(Component.text("Class: " + ec.getDisplay(), NamedTextColor.YELLOW));
		}
		List<Component> loreLines = progress != null
				? progress.buildLoreLines()
				: achievement.getDescription(0, mastery - 1);
		for (Component line : loreLines) {
			hoverText = hoverText.append(Component.newline()).append(line);
		}
		return hoverText.append(Component.newline()).append(Component.newline())
				.append(Component.text("Click to view achievements", NamedTextColor.GRAY));
	}

	private static void processToastQueue(UUID uuid) {
		Deque<ToastEntry> queue = toastQueues.get(uuid);
		if (queue == null || queue.isEmpty()) return;
		ToastEntry entry = queue.peekFirst();
		Player p = Bukkit.getPlayer(uuid);
		if (p == null || !p.isOnline()) {
			// Player offline — drain the queue
			toastQueues.remove(uuid);
			return;
		}
		showAdvancementToast(p, entry.title, entry.description, entry.icon);
		p.playSound(p, Sound.BLOCK_BEACON_ACTIVATE, 1F, 1F);
		new BukkitRunnable() {
			@Override
			public void run() {
				Deque<ToastEntry> q = toastQueues.get(uuid);
				if (q != null) {
					q.pollFirst();
					if (!q.isEmpty()) {
						processToastQueue(uuid);
					} else {
						toastQueues.remove(uuid);
					}
				}
			}
		}.runTaskLater(NeoRogue.inst(), TOAST_DURATION_TICKS);
	}

	private static final AtomicInteger toastCounter = new AtomicInteger(0);
	private static final HashMap<String, org.bukkit.advancement.Advancement> cachedToasts = new HashMap<>();

	@SuppressWarnings("deprecation")
	private static void showAdvancementToast(Player p, String title, String description, Material icon) {
		// Cache key based on display content — same toast combo reuses one advancement
		String cacheKey = title + "|" + description + "|" + icon.getKey().getKey();

		org.bukkit.advancement.Advancement advancement = cachedToasts.get(cacheKey);
		if (advancement == null) {
			NamespacedKey key = new NamespacedKey(NeoRogue.inst(), "toast_" + toastCounter.incrementAndGet());
			String json = "{"
					+ "\"criteria\":{\"trigger\":{\"trigger\":\"minecraft:impossible\"}},"
					+ "\"display\":{"
					+ "\"icon\":{\"id\":\"minecraft:" + icon.getKey().getKey() + "\"},"
					+ "\"title\":\"" + escapeJson(title) + "\","
					+ "\"description\":\"" + escapeJson(description) + "\","
					+ "\"frame\":\"task\","
					+ "\"show_toast\":true,"
					+ "\"announce_to_chat\":false,"
					+ "\"hidden\":true"
					+ "}"
					+ "}";
			advancement = Bukkit.getUnsafe().loadAdvancement(key, json);
			if (advancement == null) return;
			cachedToasts.put(cacheKey, advancement);
		}

		// Award criteria to show the toast (cheap operation)
		org.bukkit.advancement.AdvancementProgress progress = p.getAdvancementProgress(advancement);
		for (String criteria : progress.getRemainingCriteria()) {
			progress.awardCriteria(criteria);
		}

		// Revoke after a tick so the toast can be re-shown later
		final org.bukkit.advancement.Advancement adv = advancement;
		new BukkitRunnable() {
			@Override
			public void run() {
				Player online = Bukkit.getPlayer(p.getUniqueId());
				if (online != null && online.isOnline()) {
					org.bukkit.advancement.AdvancementProgress prog = online.getAdvancementProgress(adv);
					for (String criteria : prog.getAwardedCriteria()) {
						prog.revokeCriteria(criteria);
					}
				}
			}
		}.runTaskLater(NeoRogue.inst(), 20L);
	}

	private static String escapeJson(String s) {
		return s.replace("\\", "\\\\").replace("\"", "\\\"");
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
