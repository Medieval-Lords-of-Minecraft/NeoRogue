package me.neoblade298.neorogue.session.reward;

import java.math.BigDecimal;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import net.milkbowl.vault2.economy.Economy;

// Handles paying out real (VaultUnlocked) currency to party members when a run ends.
// The payout formula lives in calculateBreakdown() and is intentionally kept simple
// and centralized so the numbers can be tuned easily later.
public class RunReward {
	private static Economy economy;

	// ----- Payout tuning constants (adjust these to tune payouts) -----
	// Flat base amount awarded regardless of performance.
	private static final double WIN_BASE = 100.0;
	private static final double LOSE_BASE = 25.0;

	// Per-unit bonus values.
	private static final double NODE_BONUS = 10.0; // per node visited
	private static final double REGION_BONUS = 100.0; // per region completed

	// A loss with fewer than this many nodes visited earns nothing.
	private static final int DEATH_NODE_THRESHOLD = 5;
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

		Breakdown b = calculateBreakdown(s, won);
		String pluginName = NeoRogue.inst().getName();
		for (PlayerSessionData psd : s.getParty().values()) {
			if (b.total > 0) {
				economy.deposit(pluginName, psd.getUniqueId(), BigDecimal.valueOf(b.total));
			}

			Player p = psd.getPlayer();
			if (p != null) {
				Util.msgRaw(p, "<green>You earned <yellow>" + formatMoney(b.total) + "<green> for "
						+ (won ? "winning" : "completing") + " your run! <gray>(click the gold block for a breakdown)");
			}
		}
	}

	// Central payout formula. Edit the constants above or the math here to tune rewards.
	public static Breakdown calculateBreakdown(Session s, boolean won) {
		int nodes = s.getNodesVisited();
		int regions = s.getRegionsCompleted();
		int notoriety = s.getNotoriety();

		// A loss before reaching the node threshold earns nothing, no matter the progress.
		boolean zeroedByDeath = !won && nodes < DEATH_NODE_THRESHOLD;

		double base = won ? WIN_BASE : LOSE_BASE;
		double nodeBonus = nodes * NODE_BONUS;
		double regionBonus = regions * REGION_BONUS;
		double subtotal = base + nodeBonus + regionBonus;
		double notorietyMultiplier = s.getNotorietyMoneyMultiplier();
		double total = zeroedByDeath ? 0.0 : subtotal * notorietyMultiplier;

		return new Breakdown(won, zeroedByDeath, nodes, regions, notoriety, base, nodeBonus, regionBonus,
				subtotal, notorietyMultiplier, total);
	}

	// Sends a chat breakdown of a player's run earnings. Used by the win/lose "finances" gold block.
	public static void sendFinancesSummary(Player p, Session s, boolean won) {
		Breakdown b = calculateBreakdown(s, won);
		Util.msgRaw(p, "<gold><st>                    </st>[ <yellow>Run Finances</yellow> ]<st>                    </st>");
		if (b.zeroedByDeath) {
			Util.msgRaw(p, "<red>You fell before visiting <yellow>" + DEATH_NODE_THRESHOLD
					+ "<red> nodes, so you earned nothing this run.");
			return;
		}

		Util.msgRaw(p, "<gray>Base (" + (won ? "victory" : "completion") + "): <green>+" + formatMoney(b.base));
		Util.msgRaw(p, "<gray>Nodes visited (<white>" + b.nodesVisited + "<gray> \u00d7 " + formatMoney(NODE_BONUS)
				+ "): <green>+" + formatMoney(b.nodeBonus));
		Util.msgRaw(p, "<gray>Regions completed (<white>" + b.regionsCompleted + "<gray> \u00d7 " + formatMoney(REGION_BONUS)
				+ "): <green>+" + formatMoney(b.regionBonus));
		Util.msgRaw(p, "<gray>Subtotal: <yellow>" + formatMoney(b.subtotal));
		Util.msgRaw(p, "<gray>Notoriety bonus (<white>+" + s.getNotorietyMoneyBonusPercent()
				+ "%<gray>): <green>\u00d7" + String.format("%.2f", b.notorietyMultiplier));
		Util.msgRaw(p, "<gold>Total earned: <yellow>" + formatMoney(b.total));
	}

	private static String formatMoney(double amount) {
		if (economy != null) {
			return economy.format(NeoRogue.inst().getName(), BigDecimal.valueOf(amount));
		}
		return String.valueOf(Math.round(amount));
	}

	// Immutable breakdown of a single run's payout, used for both paying out and displaying finances.
	public static class Breakdown {
		public final boolean won, zeroedByDeath;
		public final int nodesVisited, regionsCompleted, notoriety;
		public final double base, nodeBonus, regionBonus, subtotal, notorietyMultiplier, total;

		private Breakdown(boolean won, boolean zeroedByDeath, int nodesVisited, int regionsCompleted, int notoriety,
				double base, double nodeBonus, double regionBonus, double subtotal, double notorietyMultiplier,
				double total) {
			this.won = won;
			this.zeroedByDeath = zeroedByDeath;
			this.nodesVisited = nodesVisited;
			this.regionsCompleted = regionsCompleted;
			this.notoriety = notoriety;
			this.base = base;
			this.nodeBonus = nodeBonus;
			this.regionBonus = regionBonus;
			this.subtotal = subtotal;
			this.notorietyMultiplier = notorietyMultiplier;
			this.total = total;
		}
	}
}
