package me.neoblade298.neorogue.session.reward;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import net.milkbowl.vault2.economy.Economy;

// Handles paying out real (VaultUnlocked) currency to party members when a run ends.
// The payout formula lives in calculatePayout() and is intentionally kept simple
// and centralized so the numbers can be tuned easily later.
public class RunReward {
	private static Economy economy;

	// ----- Placeholder tuning constants (adjust these to tune payouts) -----
	// Flat base amount awarded regardless of performance.
	private static final double WIN_BASE = 100.0;
	private static final double LOSE_BASE = 25.0;

	// Per-unit bonus values. These are summed into a "bonus" pool before the
	// win/lose bonus multiplier is applied.
	private static final double NOTORIETY_BONUS = 10.0; // per notoriety level
	private static final double REGION_BONUS = 25.0; // per region completed
	private static final double NODE_BONUS = 2.0; // per node visited
	private static final double ENDLESS_BONUS = 50.0; // flat if the run is endless

	// Wins apply the full bonus pool; losses only get a fraction of it.
	private static final double WIN_BONUS_MULTIPLIER = 1.0;
	private static final double LOSE_BONUS_MULTIPLIER = 0.25;

	// Optional per-flag bonuses. Add entries like FLAG_BONUSES.put("some_flag", 50.0);
	// Any listed flag the player has adds its value to the bonus pool.
	private static final Map<String, Double> FLAG_BONUSES = new LinkedHashMap<String, Double>();
	static {
		// FLAG_BONUSES.put("example_flag", 50.0);
	}
	// -----------------------------------------------------------------------

	// Hooks VaultUnlocked's economy service. Call once on plugin enable (after VaultUnlocked has loaded).
	public static boolean setupEconomy() {
		if (Bukkit.getPluginManager().getPlugin("VaultUnlocked") == null) {
			Bukkit.getLogger().warning("[NeoRogue] VaultUnlocked not found; run payouts will be disabled.");
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			Bukkit.getLogger().warning("[NeoRogue] No VaultUnlocked economy provider found; run payouts will be disabled.");
			return false;
		}
		economy = rsp.getProvider();
		return economy != null;
	}

	public static boolean isEnabled() {
		return economy != null;
	}

	// Pays out each party member the calculated amount for finishing a run.
	// won = true for a run victory, false for a run loss.
	public static void payout(Session s, boolean won) {
		if (economy == null) return;

		String pluginName = NeoRogue.inst().getName();
		for (PlayerSessionData psd : s.getParty().values()) {
			PlayerData pd = PlayerManager.getPlayerData(psd.getUniqueId());
			double amount = calculatePayout(s, pd, won);
			if (amount <= 0) continue;

			BigDecimal payment = BigDecimal.valueOf(amount);
			economy.deposit(pluginName, psd.getUniqueId(), payment);

			Player p = psd.getPlayer();
			if (p != null) {
				Util.msgRaw(p, "<green>You earned <yellow>" + economy.format(pluginName, payment) + "<green> for "
						+ (won ? "winning" : "completing") + " your run!");
			}
		}
	}

	// Central payout formula. Edit the constants above or the math here to tune rewards.
	public static double calculatePayout(Session s, PlayerData pd, boolean won) {
		double base = won ? WIN_BASE : LOSE_BASE;

		// Bonus pool built from run progress and player state.
		double bonus = 0.0;
		bonus += s.getNotoriety() * NOTORIETY_BONUS;
		bonus += s.getRegionsCompleted() * REGION_BONUS;
		bonus += s.getNodesVisited() * NODE_BONUS;
		if (s.isEndless()) bonus += ENDLESS_BONUS;
		bonus += calculateFlagBonus(pd);

		// Wins reap the full bonus pool; losses only a fraction.
		double bonusMultiplier = won ? WIN_BONUS_MULTIPLIER : LOSE_BONUS_MULTIPLIER;
		return base + (bonus * bonusMultiplier);
	}

	private static double calculateFlagBonus(PlayerData pd) {
		if (pd == null || FLAG_BONUSES.isEmpty()) return 0.0;
		double total = 0.0;
		for (Map.Entry<String, Double> entry : FLAG_BONUSES.entrySet()) {
			if (pd.hasFlag(entry.getKey())) total += entry.getValue();
		}
		return total;
	}
}
