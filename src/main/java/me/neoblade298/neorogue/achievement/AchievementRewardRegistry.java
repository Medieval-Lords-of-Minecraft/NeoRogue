package me.neoblade298.neorogue.achievement;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.shared.io.Section;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.unlock.UnlockNode.AchievementRequirement;

/**
 * Loads and dispatches command rewards for achievements from achievements.yml.
 *
 * <ul>
 *   <li>{@code global-commands}: run every time a player gains any achievement mastery tier.</li>
 *   <li>{@code rewards}: each runs its commands when the just-gained achievement tier exactly
 *       matches one of its {@code requirements} (id + class + tier) and every requirement is met
 *       at or above its tier. The exact-tier match means a reward fires once, without persistence.</li>
 * </ul>
 *
 * Command placeholders: {@code %player%} (name), {@code %uuid%}. Commands run from console.
 */
public class AchievementRewardRegistry {
	private static final List<String> globalCommands = new ArrayList<>();
	private static final Map<String, AchievementReward> rewards = new HashMap<>();
	// achievement id -> rewards that require it (so we only re-check affected rewards)
	private static final Map<String, List<AchievementReward>> rewardsByRequirement = new HashMap<>();

	private AchievementRewardRegistry() {
	}

	public static void reload() {
		globalCommands.clear();
		rewards.clear();
		rewardsByRequirement.clear();

		NeoCore.loadFiles(new File(NeoRogue.inst().getDataFolder(), "achievements.yml"), (yml, file) -> {
			List<String> global = yml.getStringList("global-commands");
			if (global != null) globalCommands.addAll(global);

			Section rewardsSec = yml.getSection("rewards");
			if (rewardsSec == null) return;
			for (String key : rewardsSec.getKeys()) {
				Section sec = rewardsSec.getSection(key);
				if (sec == null) continue;

				List<AchievementRequirement> reqs = new ArrayList<>();
				Section reqSec = sec.getSection("requirements");
				if (reqSec != null) {
					for (String reqKey : reqSec.getKeys()) {
						AchievementRequirement req = parseRequirement(key, reqKey, reqSec.getInt(reqKey, 1));
						if (req != null) reqs.add(req);
					}
				}
				if (reqs.isEmpty()) {
					Bukkit.getLogger().warning("[NeoRogue] Achievement reward '" + key
							+ "' has no requirements, skipping");
					continue;
				}

				List<String> cmds = new ArrayList<>();
				List<String> rawCmds = sec.getStringList("commands");
				if (rawCmds != null) cmds.addAll(rawCmds);

				AchievementReward reward = new AchievementReward(key, reqs, cmds);
				rewards.put(key, reward);
				for (AchievementRequirement req : reqs) {
					rewardsByRequirement.computeIfAbsent(req.id(), k -> new ArrayList<>()).add(reward);
				}
			}
		});
	}

	/**
	 * Called whenever a player gains an achievement mastery tier. Runs the global reward commands,
	 * then grants any reward whose requirements this exact gain (id + class + tier) completes.
	 */
	public static void handleAchievementGained(Player p, Achievement achievement, AchievementProgress progress) {
		if (p == null) return;

		// Global reward runs on every achievement mastery gained
		if (!globalCommands.isEmpty()) runCommands(p, globalCommands);

		// Only rewards that reference the just-gained achievement can trigger
		List<AchievementReward> candidates = rewardsByRequirement.get(achievement.getId());
		if (candidates == null || candidates.isEmpty()) return;

		PlayerData pd = PlayerManager.getPlayerData(p.getUniqueId());
		if (pd == null) return;

		String eventId = achievement.getId();
		EquipmentClass eventScope = progress.getScope(); // null = global
		int eventMastery = progress.getMastery();

		for (AchievementReward reward : candidates) {
			// This exact gain (id + class scope + tier) must match one requirement, so a reward
			// triggers only on the precise tier it asks for and never re-fires.
			boolean exact = false;
			for (AchievementRequirement req : reward.getRequirements()) {
				if (req.id().equals(eventId) && req.classScope() == eventScope && req.mastery() == eventMastery) {
					exact = true;
					break;
				}
			}
			if (!exact) continue;

			// Every requirement must be satisfied at or above its tier
			boolean all = true;
			for (AchievementRequirement req : reward.getRequirements()) {
				AchievementProgress reqProg = req.classScope() != null
						? pd.getClassAchievementProgress(req.id(), req.classScope())
						: pd.getGlobalAchievementProgress(req.id());
				if (reqProg == null || reqProg.getMastery() < req.mastery()) {
					all = false;
					break;
				}
			}
			if (all) runCommands(p, reward.getCommands());
		}
	}

	// Parses a requirement key ("id" or "id@CLASS") with its required tier. Class-scoped achievements
	// referenced without @CLASS resolve to their required class, otherwise the requirement is global.
	private static AchievementRequirement parseRequirement(String rewardKey, String key, int tier) {
		String achId;
		EquipmentClass classScope;
		int atIdx = key.indexOf('@');
		if (atIdx > 0) {
			achId = key.substring(0, atIdx).toLowerCase(Locale.ROOT);
			try {
				classScope = EquipmentClass.valueOf(key.substring(atIdx + 1).toUpperCase());
			} catch (IllegalArgumentException e) {
				Bukkit.getLogger().warning("[NeoRogue] Achievement reward '" + rewardKey
						+ "' requirement '" + key + "' has an unknown class");
				return null;
			}
		} else {
			achId = key.toLowerCase(Locale.ROOT);
			Achievement ach = AchievementManager.get(achId);
			classScope = ach != null ? ach.getRequiredClass() : null;
		}
		if (AchievementManager.get(achId) == null) {
			Bukkit.getLogger().warning("[NeoRogue] Achievement reward '" + rewardKey
					+ "' requires unknown achievement '" + achId + "'");
		}
		return new AchievementRequirement(achId, tier, classScope);
	}

	private static void runCommands(Player p, List<String> commands) {
		for (String cmd : commands) {
			String parsed = cmd.replace("%player%", p.getName()).replace("%uuid%", p.getUniqueId().toString());
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
		}
	}
}
