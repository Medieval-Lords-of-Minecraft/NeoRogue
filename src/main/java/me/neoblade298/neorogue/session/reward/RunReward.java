package me.neoblade298.neorogue.session.reward;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.Cargo;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.Session;
import net.milkbowl.vault2.economy.Economy;

// Handles paying out real (VaultUnlocked) currency to party members when a run ends.
// The payout formula lives in calculateBreakdown() and is intentionally kept simple
// and centralized so the numbers can be tuned easily later.
public class RunReward {
	private static Economy economy;

	// ----- Payout tuning constants (adjust these to tune payouts) -----
	// Flat base amount awarded regardless of performance.
	private static final double WIN_BASE = 1000.0;
	private static final double LOSE_BASE = 25.0;

	// Per-unit bonus values.
	private static final double NODE_BONUS = 10.0; // per node visited
	private static final double REGION_BONUS = 100.0; // per region completed

	// A loss with fewer than this many nodes visited earns nothing.
	private static final int DEATH_NODE_THRESHOLD = 5;

	// Random +/- variance applied to each region's cargo sell percentage at runtime.
	private static final double CARGO_SELL_VARIANCE = 0.03;
	// -----------------------------------------------------------------------

	// Hooks VaultUnlocked's economy service. Call once on plugin enable (after VaultUnlocked has loaded).
	public static boolean setupEconomy() {
		if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
			Bukkit.getLogger().warning("[NeoRogue] Vault not found; run payouts will be disabled.");
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			Bukkit.getLogger().warning("[NeoRogue] No Vault economy provider found; run payouts will be disabled.");
			return false;
		}
		economy = rsp.getProvider();
		return economy != null;
	}

	public static boolean isEnabled() {
		return economy != null;
	}

	// Deposits arbitrary VaultUnlocked currency to a party member (used for cargo sale proceeds).
	public static void depositCargo(PlayerSessionData psd, double amount) {
		if (economy == null || amount <= 0) return;
		economy.deposit(NeoRogue.inst().getName(), psd.getUniqueId(), BigDecimal.valueOf(amount));
	}

	// Whether the player can afford the given amount of VaultUnlocked currency.
	public static boolean hasBalance(java.util.UUID uuid, double amount) {
		if (economy == null) return false;
		return economy.has(NeoRogue.inst().getName(), uuid, BigDecimal.valueOf(amount));
	}

	// Deposits currency directly to a player by uuid (used for fleet earnings collected outside a run).
	public static boolean deposit(java.util.UUID uuid, double amount) {
		if (economy == null || amount <= 0) return false;
		return economy.deposit(NeoRogue.inst().getName(), uuid, BigDecimal.valueOf(amount)).transactionSuccess();
	}

	// Attempts to charge the player. Returns true only if the funds were successfully withdrawn.
	public static boolean charge(java.util.UUID uuid, double amount) {
		if (economy == null || amount <= 0) return false;
		String pluginName = NeoRogue.inst().getName();
		if (!economy.has(pluginName, uuid, BigDecimal.valueOf(amount))) return false;
		return economy.withdraw(pluginName, uuid, BigDecimal.valueOf(amount)).transactionSuccess();
	}

	// Cargo sales count as run-reward "base" income: the raw sell value is multiplied by the notoriety
	// money multiplier (the same bonus the end-of-run base earns). Because cargo is sold once per
	// completed region (and again on victory), each sale is effectively its own run reward paid out
	// during the run. Returns the actual amount deposited (post-multiplier).
	private static double payoutCargoReward(Session s, PlayerSessionData psd, double cargoValue) {
		PlayerData pd = psd.getData();
		double sellMult = pd != null ? pd.getSellMultiplier() : 1.0;
		double reward = cargoValue * s.getNotorietyMoneyMultiplier() * sellMult;
		depositCargo(psd, reward);
		return reward;
	}

	// Pays each party member their persistent per-region completion reward (a caravan upgrade). Scaled
	// by the notoriety money multiplier like other cargo income. Called when a region is completed.
	public static void awardRegionBaseReward(Session s) {
		for (PlayerSessionData psd : s.getParty().values()) {
			PlayerData pd = psd.getData();
			if (pd == null) continue;
			int base = pd.getCargoBaseReward();
			if (base <= 0) continue;
			double reward = base * s.getNotorietyMoneyMultiplier();
			depositCargo(psd, reward);
			Player p = psd.getPlayer();
			if (p != null) {
				Util.msgRaw(p, "<gray>Your caravan earned a <yellow>" + formatMoney(reward)
						+ "</yellow> reward for completing the region!");
			}
		}
	}

	// Prints the per-material breakdown (most valuable first) for a cargo sale, then a notoriety-bonus
	// note whenever the multiplier boosted the payout above the raw sale value.
	private static void sendCargoBreakdown(Player p, Session s, PlayerSessionData.CargoSaleResult result, double reward) {
		List<Map.Entry<Material, Integer>> lines = new ArrayList<Map.Entry<Material, Integer>>(
				result.qtyByMaterial.entrySet());
		lines.sort(Comparator.comparingDouble(
				(Map.Entry<Material, Integer> e) -> result.valueByMaterial.getOrDefault(e.getKey(), 0.0))
				.reversed());
		for (Map.Entry<Material, Integer> line : lines) {
			Material mat = line.getKey();
			Util.msgRaw(p, "<gray>  - <white>" + line.getValue() + "x <yellow>" + prettyName(mat)
					+ " <gray>for <yellow>" + formatMoney(result.valueByMaterial.getOrDefault(mat, 0.0)));
		}
		if (reward > result.value) {
			Util.msgRaw(p, "<gray>  <white>(includes <green>+" + s.getNotorietyMoneyBonusPercent()
					+ "%<white> notoriety bonus on the <yellow>" + formatMoney(result.value) + "<white> sale value)");
		}
	}


	// Sells each party member's run cargo for a completed region. The region's base percentage is
	// randomized by +/- CARGO_SELL_VARIANCE, proceeds are paid out immediately, and the sale is
	// logged on each PlayerSessionData for the end-of-run finance summary.
	public static void sellCargoForRegion(Session s, RegionType completed) {
		double base = completed.getCargoSellPercent();
		if (base <= 0) return;
		for (PlayerSessionData psd : s.getParty().values()) {
			if (psd.getRunCargoTotal() <= 0) continue;
			double variance = (NeoRogue.gen.nextDouble() * 2 - 1) * CARGO_SELL_VARIANCE;
			double fraction = Math.max(0.0, Math.min(1.0, base + variance));
			PlayerSessionData.CargoSaleResult result = psd.sellRunCargo(fraction);
			if (result.itemsSold <= 0) continue;
			double reward = payoutCargoReward(s, psd, result.value);
			Player p = psd.getPlayer();
			if (p != null) {
				Util.msgRaw(p, "<gray>Your caravan sold <yellow>" + result.itemsSold + "</yellow> cargo item"
						+ (result.itemsSold == 1 ? "" : "s") + " in " + completed.getDisplay() + " for <yellow>"
						+ formatMoney(reward) + "</yellow>:");

				// Per-material breakdown, most valuable first, plus any notoriety bonus applied.
				sendCargoBreakdown(p, s, result, reward);
			}
		}
	}

	// Pays out each party member the calculated amount for finishing a run.
	// won = true for a run victory, false for a run loss.
	public static void payout(Session s, boolean won) {
		// On a victory the caravan reaches safety and sells all remaining cargo at full value,
		// ignoring region sell rates. Runs before returnUnsoldCargo so nothing is left to return.
		if (won) sellRemainingCargo(s);
		returnUnsoldCargo(s, won);
		if (economy == null) return;

		Breakdown b = calculateBreakdown(s, won);
		String pluginName = NeoRogue.inst().getName();
		for (PlayerSessionData psd : s.getParty().values()) {
			if (b.total > 0) {
				economy.deposit(pluginName, psd.getUniqueId(), BigDecimal.valueOf(b.total));
			}

			Player p = psd.getPlayer();
			if (p != null) {
				Util.msgRaw(p, "<gray>You earned <yellow>" + formatMoney(b.total) + "</yellow> for "
						+ (won ? "winning" : "completing") + " your run! (click the gold block for a breakdown)");
			}
		}
	}

	// On a run victory, sells every party member's remaining run cargo at full value regardless of
	// the region's sell rate. Proceeds are paid out immediately and recorded for the finance summary.
	public static void sellRemainingCargo(Session s) {
		for (PlayerSessionData psd : s.getParty().values()) {
			if (psd.getRunCargoTotal() <= 0) continue;
			PlayerSessionData.CargoSaleResult result = psd.sellRunCargo(1.0);
			if (result.itemsSold <= 0) continue;
			double reward = payoutCargoReward(s, psd, result.value);
			Player p = psd.getPlayer();
			if (p != null) {
				Util.msgRaw(p, "<gray>Your caravan reached safety and sold its remaining <yellow>" + result.itemsSold
						+ "</yellow> cargo item" + (result.itemsSold == 1 ? "" : "s") + " for <yellow>"
						+ formatMoney(reward) + "</yellow>:");

				// Per-material breakdown, most valuable first, plus any notoriety bonus applied.
				sendCargoBreakdown(p, s, result, reward);
			}
		}
	}

	// At run end, returns each player's unsold run cargo to their persistent cargo. Anything that no
	// longer fits overflows into their lost cargo; anything still left over is discarded. On a loss the
	// run cargo is only kept if the player has caravan insurance; otherwise it is discarded entirely.
	public static void returnUnsoldCargo(Session s, boolean won) {
		for (PlayerSessionData psd : s.getParty().values()) {
			Map<Material, Integer> remaining = psd.getRunCargo();
			if (remaining.isEmpty()) continue;
			PlayerData pd = psd.getData();
			if (pd == null) continue;
			// Without caravan insurance, unsold run cargo is lost when the run ends in defeat.
			if (!won && !pd.hasFlag(PlayerData.FLAG_CARGO_INSURANCE)) {
				remaining.clear();
				Player p = psd.getPlayer();
				if (p != null) Util.msgRaw(p, "<red>Without caravan insurance, your unsold cargo was lost!");
				continue;
			}
			Cargo cargo = pd.getCargo();
			Cargo lost = pd.getLostCargo();
			boolean anyDiscarded = false;
			for (Map.Entry<Material, Integer> ent : new HashMap<Material, Integer>(remaining).entrySet()) {
				Material mat = ent.getKey();
				int leftover = ent.getValue() - cargo.addItem(mat, ent.getValue());
				if (leftover > 0) {
					leftover -= lost.addItem(mat, leftover);
					if (leftover > 0) anyDiscarded = true;
				}
			}
			remaining.clear();
			pd.saveCargoAsync();
			pd.saveLostCargoAsync();
			Player p = psd.getPlayer();
			if (p != null && anyDiscarded) {
				Util.msgRaw(p, "<red>Some unsold cargo didn't fit in your cargo or lost cargo and was discarded!");
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

		// Cargo is sold and paid out per region during the run; summarize what each player sold here.
		// Cargo income counts as run-reward base, so the notoriety multiplier applies to it too.
		PlayerSessionData psd = s.getParty().get(p.getUniqueId());
		if (psd != null && psd.hasSoldCargo()) {
			Util.msgRaw(p, "<gold><st>          </st>[ <yellow>Cargo Sold</yellow> ]<st>          </st>");
			double cargoTotal = 0;
			for (Map.Entry<Material, Integer> ent : psd.getSoldCargoQty().entrySet()) {
				Material mat = ent.getKey();
				int qty = ent.getValue();
				double value = psd.getSoldCargoValue().getOrDefault(mat, 0.0);
				cargoTotal += value;
				Util.msgRaw(p, "<gray>  " + prettyName(mat) + " <white>\u00d7" + qty + " <gray>\u2192 <yellow>"
						+ formatMoney(value));
			}
			double cargoMultiplier = s.getNotorietyMoneyMultiplier();
			double cargoReward = cargoTotal * cargoMultiplier;
			if (cargoReward > cargoTotal) {
				Util.msgRaw(p, "<gray>Sale value: <yellow>" + formatMoney(cargoTotal));
				Util.msgRaw(p, "<gray>Notoriety bonus (<white>+" + s.getNotorietyMoneyBonusPercent()
						+ "%<gray>): <green>\u00d7" + String.format("%.2f", cargoMultiplier));
			}
			Util.msgRaw(p, "<gold>Total cargo earned: <yellow>" + formatMoney(cargoReward));
		}
	}

	// Converts an enum material name (e.g. IRON_INGOT) to a readable label (e.g. Iron Ingot).
	private static String prettyName(Material mat) {
		String[] parts = mat.name().toLowerCase().split("_");
		StringBuilder sb = new StringBuilder();
		for (String part : parts) {
			if (part.isEmpty()) continue;
			if (sb.length() > 0) sb.append(' ');
			sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
		}
		return sb.toString();
	}

	public static String formatMoney(double amount) {
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
