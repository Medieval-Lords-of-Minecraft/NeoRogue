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
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.advancements.Advancement;
import com.github.retrooper.packetevents.protocol.advancements.AdvancementDisplay;
import com.github.retrooper.packetevents.protocol.advancements.AdvancementHolder;
import com.github.retrooper.packetevents.protocol.advancements.AdvancementProgress;
import com.github.retrooper.packetevents.protocol.advancements.AdvancementProgress.CriterionProgress;
import com.github.retrooper.packetevents.protocol.advancements.AdvancementType;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAdvancements;

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
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

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
			new AllMinibossesAchievement("all_minis_ld", Component.text("Low District Miniboss Slayer", NamedTextColor.GOLD),
					Material.STONE_SWORD, RegionType.LOW_DISTRICT),
			new AllMinibossesAchievement("all_minis_hf", Component.text("Harvest Fields Miniboss Slayer", NamedTextColor.GOLD),
					Material.STONE_SWORD, RegionType.HARVEST_FIELDS),
			new AllMinibossesAchievement("all_minis_fw", Component.text("Frozen Wastes Miniboss Slayer", NamedTextColor.GOLD),
					Material.STONE_SWORD, RegionType.FROZEN_WASTES),
			new AllBossesAchievement("all_bosses_ld", Component.text("Low District Boss Slayer", NamedTextColor.GOLD),
					Material.DIAMOND_SWORD, RegionType.LOW_DISTRICT),
			new AllBossesAchievement("all_bosses_hf", Component.text("Harvest Fields Boss Slayer", NamedTextColor.GOLD),
					Material.DIAMOND_SWORD, RegionType.HARVEST_FIELDS),
			new AllBossesAchievement("all_bosses_fw", Component.text("Frozen Wastes Boss Slayer", NamedTextColor.GOLD),
					Material.DIAMOND_SWORD, RegionType.FROZEN_WASTES),
			new NoHealthLossAchievement(),
			new FlawlessRegionAchievement(),
			new MaxStatAchievement("max_hp_200", Component.text("Beefy", NamedTextColor.GOLD),
					Material.GOLDEN_APPLE, StatType.HEALTH, 200),
			new MaxStatAchievement("max_mana_100", Component.text("Arcane Reservoir", NamedTextColor.GOLD),
					Material.LAPIS_LAZULI, StatType.MANA, 100),
			new MaxStatAchievement("max_stamina_100", Component.text("Tireless", NamedTextColor.GOLD),
					Material.FEATHER, StatType.STAMINA, 100),
			new AcquireRarityAchievement("acquire_rare", Component.text("Rare Find", NamedTextColor.GOLD),
					Material.GOLD_INGOT, Rarity.RARE),
			new AcquireRarityAchievement("acquire_epic", Component.text("Epic Discovery", NamedTextColor.GOLD),
					Material.DIAMOND, Rarity.EPIC),
			new VisitNodesAchievement(),
			new SpendCoinsAchievement(),
			new SRankRegionAchievement("srank_ld", Component.text("Low District Speedster", NamedTextColor.GOLD),
					Material.CLOCK, RegionType.LOW_DISTRICT),
			new SRankRegionAchievement("srank_hf", Component.text("Harvest Fields Speedster", NamedTextColor.GOLD),
					Material.CLOCK, RegionType.HARVEST_FIELDS),
			new SRankRegionAchievement("srank_fw", Component.text("Frozen Wastes Speedster", NamedTextColor.GOLD),
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
		sendToast(p, achievement, mastery, ec);
		achievement.grantReward(p, mastery, ec);
	}

	public static void sendToast(Player p, Achievement achievement, int mastery, EquipmentClass ec) {
		String displayName = PlainTextComponentSerializer.plainText().serialize(achievement.getDisplayName());
		String description = "Mastery " + mastery + "/" + achievement.getMasteryThresholds().length;
		if (ec != null) {
			description += " [" + ec.getDisplay() + "]";
		}
		showAdvancementToast(p, displayName, description, achievement.getMaterial());
		p.playSound(p, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1F, 1F);
		Component chatMsg = Component.text("[Achievement] ", NamedTextColor.GREEN)
				.append(achievement.getDisplayName())
				.append(Component.text(" (" + mastery + "/" + achievement.getMasteryThresholds().length + ")", NamedTextColor.GOLD));
		if (ec != null) {
			chatMsg = chatMsg.append(Component.text(" [" + ec.getDisplay() + "]", NamedTextColor.YELLOW));
		}
		p.sendMessage(chatMsg);
	}

	private static final AtomicInteger toastCounter = new AtomicInteger(0);

	private static void showAdvancementToast(Player p, String title, String description, Material icon) {
		ResourceLocation key = new ResourceLocation("neorogue", "toast_" + toastCounter.incrementAndGet());

		// Build display
		AdvancementDisplay display = new AdvancementDisplay(
				Component.text(title),
				Component.text(description),
				ItemStack.builder().type(ItemTypes.getByName("minecraft:" + icon.getKey().getKey())).build(),
				AdvancementType.CHALLENGE,
				null, // no background
				true, // showToast
				true, // hidden
				0f, 0f
		);

		// Build advancement and holder
		Advancement adv = new Advancement(null, display, List.of(List.of("trigger")), false);
		AdvancementHolder holder = new AdvancementHolder(key, adv);

		// Build progress marking the criterion as completed
		Map<ResourceLocation, AdvancementProgress> progressMap = Map.of(
				key, new AdvancementProgress(Map.of("trigger", new CriterionProgress(System.currentTimeMillis())))
		);

		// Send the advancement + progress packet (shows the toast)
		WrapperPlayServerUpdateAdvancements addPacket = new WrapperPlayServerUpdateAdvancements(
				false, List.of(holder), Set.of(), progressMap, false
		);
		PacketEvents.getAPI().getPlayerManager().sendPacket(p, addPacket);

		// Schedule removal so it doesn't persist in the advancement screen
		new BukkitRunnable() {
			@Override
			public void run() {
				WrapperPlayServerUpdateAdvancements removePacket = new WrapperPlayServerUpdateAdvancements(
						false, List.of(), Set.of(key), Map.of(), false
				);
				PacketEvents.getAPI().getPlayerManager().sendPacket(p, removePacket);
			}
		}.runTaskLater(NeoRogue.inst(), 20L);
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
