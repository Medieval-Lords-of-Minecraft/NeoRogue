package me.neoblade298.neorogue.session.reward;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.Cargo;
import me.neoblade298.neorogue.player.CargoItem;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.PlayerSessionInventory;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.milkbowl.vault2.economy.Economy;

// Handles paying out real (VaultUnlocked) currency to party members when a run ends.
// The payout formula lives in calculateBreakdown() and is intentionally kept simple
// and centralized so the numbers can be tuned easily later.
public class RunReward {
	private static Economy economy;

	// ----- Payout tuning constants (adjust these to tune payouts) -----
	// Per-node loss reward. Base win rewards are configured on RegionType.
	private static final double NODE_BONUS = 50.0; // per node visited

	// Additional payout multiplier granted per party member beyond the first (e.g. 0.10 = +10% each).
	private static final double PARTY_SIZE_BONUS = 0.10;

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
		if (economy.deposit(NeoRogue.inst().getName(), psd.getUniqueId(), BigDecimal.valueOf(amount)).transactionSuccess()) {
			psd.trigger(SessionTrigger.CROWN_EARNED, amount);
		}
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
		// Notoriety and cargo sale bonuses stack additively rather than multiplicatively:
		// e.g. +50% notoriety and +20% sell bonus yield a x1.7 multiplier (not x1.8).
		double normalReward = cargoValue * (s.getNotorietyMoneyMultiplier() + sellMult - 1.0);
		double reward = applyCurrencyBoost(psd, normalReward);
		depositCargo(psd, reward);
		return reward;
	}

	private static double applyCurrencyBoost(PlayerSessionData psd, double normalReward) {
		return normalReward * psd.getRunCurrencyBoostMultiplier();
	}

	// Builds the per-variant raw sale-value breakdown shown when a player hovers a cargo summary line.
	// Multipliers are detailed separately by buildMultHover().
	private static String buildCargoHover(PlayerSessionData.CargoSaleResult result) {
		List<Map.Entry<CargoItem, Integer>> lines = new ArrayList<Map.Entry<CargoItem, Integer>>(
				result.qtyByItem.entrySet());
		lines.sort(Comparator.comparingDouble(
				(Map.Entry<CargoItem, Integer> e) -> result.valueByItem.getOrDefault(e.getKey(), 0.0))
				.reversed());
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Map.Entry<CargoItem, Integer> line : lines) {
			CargoItem item = line.getKey();
			if (!first) sb.append("<newline>");
			first = false;
			sb.append("<white>").append(line.getValue()).append("x <yellow>").append(item.getLabel())
					.append(" <gray>for <yellow>").append(formatMoney(result.valueByItem.getOrDefault(item, 0.0)));
		}
		return sb.toString();
	}

	// Wraps the "<count> cargo item(s)" phrase in an underlined hover that reveals the sale breakdown.
	private static String hoverableCargoItems(String hover, int count) {
		return "<hover:show_text:'" + hover + "'><yellow><underlined>" + count + "</underlined> cargo item"
				+ (count == 1 ? "" : "s") + "</hover>";
	}

	// Formats the effective payout multiplier (e.g. 1.65) applied to a cargo sale's raw value.
	private static String formatMult(double mult) {
		return new java.text.DecimalFormat("0.##").format(mult);
	}

	private static boolean isRelevantMultiplier(double mult) {
		return Math.abs(mult - 1.0) > 0.000001;
	}

	// Builds the hover text breaking the effective cargo sale multiplier into its component sources
	// (notoriety money bonus and the player's caravan sell multiplier).
	private static String buildMultHover(Session s, PlayerSessionData psd) {
		PlayerData pd = psd != null ? psd.getData() : null;
		int notorietyPct = s.getNotorietyMoneyBonusPercent();
		int sellPct = pd != null ? pd.getSellMultiplierBonus() : 0;
		StringBuilder sb = new StringBuilder();
		sb.append("<gray>Base sale value <yellow>\u00d71");
		if (notorietyPct != 0) {
			sb.append("<newline><white>+").append(notorietyPct).append("%<gray> notoriety bonus");
		}
		if (sellPct != 0) {
			sb.append("<newline><white>+").append(sellPct).append("%<gray> caravan sell bonus");
		}
		if (!psd.getRunCurrencyBoosts().isEmpty()) {
			double normalMultiplier = s.getNotorietyMoneyMultiplier() + (pd != null ? pd.getSellMultiplier() : 1.0) - 1.0;
			sb.append("<newline><gray>Normal sale reward <yellow>\u00d7").append(formatMult(normalMultiplier));
			for (PlayerSessionData.RunCurrencyBoost boost : psd.getRunCurrencyBoosts()) {
				sb.append("<newline><white>+").append(Math.round(boost.bonus() * 100)).append("%<gray> ")
						.append(boost.displayName());
			}
			sb.append("<newline><gray>Currency boost <yellow>\u00d7")
					.append(formatMult(psd.getRunCurrencyBoostMultiplier()));
		}
		if (notorietyPct == 0 && sellPct == 0 && psd.getRunCurrencyBoosts().isEmpty()) {
			sb.append("<newline><gray>No bonuses applied");
		}
		return sb.toString();
	}

	// Wraps the effective payout multiplier in an underlined hover revealing its component sources.
	private static String hoverableMult(Session s, PlayerSessionData psd, double mult) {
		return "<hover:show_text:'" + buildMultHover(s, psd) + "'><green><underlined>\u00d7" + formatMult(mult)
				+ "</underlined></green>";
	}

	private static String currencyBoostReceiptLine(PlayerSessionData psd) {
		if (!isRelevantMultiplier(psd.getRunCurrencyBoostMultiplier())) return "";
		StringBuilder names = new StringBuilder();
		for (PlayerSessionData.RunCurrencyBoost boost : psd.getRunCurrencyBoosts()) {
			if (names.length() > 0) names.append(", ");
			names.append(boost.displayName()).append(" +").append(Math.round(boost.bonus() * 100)).append('%');
		}
		if (names.length() == 0) names.append("none");
		return "<gray>Currency boost (<white>" + names + "<gray>): <green>\u00d7"
				+ formatMult(psd.getRunCurrencyBoostMultiplier());
	}


	// Called when a region is completed: pays its base and node rewards, auto-sells a portion of each
	// player's run cargo, and awards their caravan completion reward in one detailed receipt.
	public static void awardRegionCompletion(Session s, RegionType completed) {
		awardRegionCompletion(s, completed, completed.getCargoSellPercent(), true);
	}

	private static void awardRegionCompletion(Session s, RegionType completed, double sellPercent, boolean announce) {
		double partyMultiplier = 1.0 + getPartyMoneyBonusPercent(s) / 100.0;
		double standardMultiplier = s.getNotorietyMoneyMultiplier() * partyMultiplier;
		double normalBaseReward = completed.getRegionReward() * standardMultiplier;
		for (PlayerSessionData psd : s.getParty().values()) {
			PlayerData pd = psd.getData();
			double baseReward = applyCurrencyBoost(psd, normalBaseReward);

			// Persistent caravan completion reward, independent of the standard region reward.
			double caravanReward = 0;
			if (pd != null && pd.getCargoBaseReward() > 0) {
				caravanReward = applyCurrencyBoost(psd,
						pd.getCargoBaseReward() * s.getNotorietyMoneyMultiplier());
			}

			// Auto-sell a portion of the player's run cargo (skipped if the region doesn't sell cargo).
			PlayerSessionData.CargoSaleResult result = null;
			double cargoReward = 0;
			if (sellPercent > 0 && psd.getRunCargoTotal() > 0) {
				double variance = sellPercent >= 1.0 ? 0.0
						: (NeoRogue.gen.nextDouble() * 2 - 1) * CARGO_SELL_VARIANCE;
				double fraction = Math.max(0.0, Math.min(1.0, sellPercent + variance));
				PlayerSessionData.CargoSaleResult sale = psd.sellRunCargo(fraction);
				if (sale.itemsSold > 0) {
					result = sale;
					cargoReward = payoutCargoReward(s, psd, sale.value);
				}
			}
			double total = baseReward + caravanReward + cargoReward;
			depositCargo(psd, baseReward + caravanReward);

			Player p = psd.getPlayer();
			if (p == null) continue;

			if (result != null) PlayerSessionInventory.updateCargoIcon(psd);
			if (!announce) continue;

			String cargoLine = "";
			if (result != null) {
				double mult = result.value > 0 ? cargoReward / result.value : 1.0;
				cargoLine = "<gray>Cargo sales ("
						+ hoverableCargoItems(buildCargoHover(result), result.itemsSold);
				if (isRelevantMultiplier(mult)) cargoLine += ", " + hoverableMult(s, psd, mult) + "<gray>";
				cargoLine += "): <yellow>" + formatMoney(cargoReward) + "<newline>";
			}
			String regionMultiplierLine = isRelevantMultiplier(standardMultiplier)
					? "<gray>Region reward multiplier: <green>\u00d7" + String.format("%.2f", standardMultiplier)
							+ "<newline>"
					: "";
			String currencyLine = currencyBoostReceiptLine(psd);
			if (!currencyLine.isEmpty()) currencyLine += "<newline>";
			String caravanLine = caravanReward > 0
					? "<gray>Caravan completion reward: <yellow>" + formatMoney(caravanReward) + "<newline>"
					: "";
			Util.msgRaw(p, "<gold><bold>" + completed.getDisplay() + " Rewards</bold><newline>"
					+ "<gray>Base region reward: <yellow>" + formatMoney(baseReward) + "<newline>"
					+ regionMultiplierLine
					+ currencyLine
					+ caravanLine
					+ cargoLine + "<gold>Total: <yellow>" + formatMoney(total));
		}
	}

	// Pays out each party member the calculated amount for finishing a run.
	// won = true for a run victory, false for a run loss.
	public static void payout(Session s, boolean won) {
		// The final region has no RewardInstance, so pay and announce its complete region reward here.
		// A 100% sell rate ensures the receipt itemizes every cargo item left at victory.
		if (won) {
			awardRegionCompletion(s, s.getRegion().getType(), 1.0, true);
		}
		returnUnsoldCargo(s, won);
		if (economy == null) return;

		Breakdown b = calculateBreakdown(s, won);
		for (PlayerSessionData psd : s.getParty().values()) {
			double total = applyCurrencyBoost(psd, b.total);
			if (total > 0) {
				depositCargo(psd, total);
			}

			Player p = psd.getPlayer();
			if (p != null) {
				if (won) {
					Util.msgRaw(p, "<gray>Total experience earned: <green>"
							+ psd.getSessionStats().getExpEarned() + " exp");
				}
				else {
					String expSummary = "</yellow> and <green>" + psd.getSessionStats().getExpEarned()
							+ " exp</green>";
					Util.msgRaw(p, "<gray>You earned <yellow>" + formatMoney(total) + expSummary
							+ " for completing your run!");
				}
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
			PlayerSessionInventory.updateCargoIcon(psd);
			Player p = psd.getPlayer();
			if (p != null) {
				double mult = result.value > 0 ? reward / result.value : 1.0;
				String multText = isRelevantMultiplier(mult)
						? " <gray>(" + hoverableMult(s, psd, mult) + "<gray>)"
						: "";
				Util.msgRaw(p, "<gray>Your caravan reached safety and sold its remaining "
						+ hoverableCargoItems(buildCargoHover(result), result.itemsSold) + " for <yellow>"
						+ formatMoney(reward) + "</yellow>" + multText + "<gray>.");
			}
		}
	}

	// At run end, returns each player's unsold run cargo to their persistent cargo. Anything that no
	// longer fits overflows into their lost cargo; anything still left over is discarded. On a loss the
	// run cargo is only kept if the player has caravan insurance; otherwise it is discarded entirely.
	public static void returnUnsoldCargo(Session s, boolean won) {
		for (PlayerSessionData psd : s.getParty().values()) {
			Map<CargoItem, Integer> remaining = psd.getRunCargo();
			if (remaining.isEmpty()) continue;
			PlayerData pd = psd.getData();
			if (pd == null) continue;
			// Without caravan insurance, unsold run cargo is lost when the run ends in defeat.
			if (!won && !pd.hasFlag(PlayerData.FLAG_CARGO_INSURANCE)) {
				remaining.clear();
				PlayerSessionInventory.updateCargoIcon(psd);
				Player p = psd.getPlayer();
				if (p != null) Util.msgRaw(p, "<red>Without caravan insurance, your unsold cargo was lost!");
				continue;
			}
			Cargo cargo = pd.getCargo();
			Cargo lost = pd.getLostCargo();
			boolean anyDiscarded = false;
			for (Map.Entry<CargoItem, Integer> ent : new HashMap<CargoItem, Integer>(remaining).entrySet()) {
				CargoItem item = ent.getKey();
				int leftover = ent.getValue() - cargo.addItem(item, ent.getValue());
				if (leftover > 0) {
					leftover -= lost.addItem(item, leftover);
					if (leftover > 0) anyDiscarded = true;
				}
			}
			remaining.clear();
			PlayerSessionInventory.updateCargoIcon(psd);
			pd.saveCargoAsync();
			pd.saveLostCargoAsync();
			Player p = psd.getPlayer();
			if (p != null && anyDiscarded) {
				Util.msgRaw(p, "<red>Some unsold cargo didn't fit in your cargo or lost cargo and was discarded!");
			}
		}
	}

	// Calculates only the reward paid on the end screen. Wins have no separate final payout because
	// their base rewards come from completed regions; losses earn NODE_BONUS for every node visited.
	public static Breakdown calculateBreakdown(Session s, boolean won) {
		int nodes = s.getNodesVisited();
		int regions = s.getRegionsCompleted();
		int notoriety = s.getNotoriety();

		// A loss before reaching the node threshold earns nothing, no matter the progress.
		boolean zeroedByDeath = !won && nodes < DEATH_NODE_THRESHOLD;

		double base = 0;
		double nodeBonus = won ? 0 : nodes * NODE_BONUS;
		double regionBonus = 0;
		double subtotal = base + nodeBonus + regionBonus;
		double notorietyMultiplier = s.getNotorietyMoneyMultiplier();
		// +PARTY_SIZE_BONUS per party member beyond the first (solo runs are unaffected).
		int partySize = s.getParty().size();
		double partyMultiplier = 1.0 + getPartyMoneyBonusPercent(s) / 100.0;
		double total = zeroedByDeath ? 0.0 : subtotal * notorietyMultiplier * partyMultiplier;

		return new Breakdown(won, zeroedByDeath, nodes, regions, notoriety, base, nodeBonus, regionBonus,
				subtotal, notorietyMultiplier, partySize, partyMultiplier, total);
	}

	public static int getPartyMoneyBonusPercent(Session s) {
		return getPartyMoneyBonusPercent(s.getParty().size());
	}

	public static int getPartyMoneyBonusPercent(int partySize) {
		return (int) Math.round(PARTY_SIZE_BONUS * Math.max(0, partySize - 1) * 100);
	}

	private static double getCompletedRegionRewardBase(Session s, boolean won) {
		RegionType region = won ? s.getRegion().getType()
				: RegionType.getPreviousRegion(s.getRegion().getType());
		double total = 0;
		for (int remaining = s.getRegionsCompleted(); remaining > 0 && region != null; remaining--) {
			total += region.getRegionReward();
			region = RegionType.getPreviousRegion(region);
		}
		return total;
	}

	// Builds the complete run-finances summary. Prior region rewards are reconstructed from the
	// session counters, while personal caravan and cargo totals use the viewer's aggregate data.
	public static List<Component> buildFinancesLore(Session s, PlayerSessionData psd, boolean won) {
		List<Component> lore = new ArrayList<Component>();
		Breakdown b = calculateBreakdown(s, won);
		double currencyMultiplier = psd == null ? 1.0 : psd.getRunCurrencyBoostMultiplier();
		double finalReward = b.total * currencyMultiplier;
		if (!won) {
			lore.add(loreLine("<gold>Loss Reward"));
			if (b.zeroedByDeath) {
				lore.add(loreLine("<red>Fewer than <yellow>" + DEATH_NODE_THRESHOLD
						+ "<red> nodes visited; no loss reward."));
			}
			else {
				lore.add(loreLine("<gray>Nodes visited (<white>" + b.nodesVisited + "<gray> \u00d7 "
						+ formatWholeMoney(NODE_BONUS) + "): <green>+" + formatWholeMoney(b.nodeBonus)));
				lore.add(loreLine("<gray>Subtotal: <yellow>" + formatWholeMoney(b.subtotal)));
			}
			if (isRelevantMultiplier(b.notorietyMultiplier)) {
				lore.add(loreLine("<gray>Notoriety bonus (<white>+" + s.getNotorietyMoneyBonusPercent()
						+ "%<gray>): <green>\u00d7" + String.format("%.2f", b.notorietyMultiplier)));
			}
			if (b.partySize > 1) {
				lore.add(loreLine("<gray>Party bonus (<white>" + b.partySize + "<gray> players, <white>+"
						+ Math.round(PARTY_SIZE_BONUS * 100) + "%<gray> each beyond the first): <green>\u00d7"
						+ String.format("%.2f", b.partyMultiplier)));
			}
			if (psd != null) addCurrencyBoostLore(lore, psd);
			lore.add(loreLine("<gold>Loss reward earned: <yellow>" + formatWholeMoney(finalReward)));
			lore.add(Component.empty());
		}

		lore.add(loreLine("<gold>Rewards Paid Throughout Run"));
		double standardMultiplier = b.notorietyMultiplier * b.partyMultiplier;
		double regionBaseReward = getCompletedRegionRewardBase(s, won) * standardMultiplier * currencyMultiplier;
		lore.add(loreLine("<gray>Completed regions (<white>" + b.regionsCompleted + "<gray>): <green>+"
				+ formatWholeMoney(regionBaseReward)));

		if (psd != null) {
			PlayerData pd = psd.getData();
			double caravanReward = pd == null ? 0 : b.regionsCompleted * pd.getCargoBaseReward()
					* b.notorietyMultiplier * currencyMultiplier;
			if (caravanReward > 0) {
				lore.add(loreLine("<gray>Caravan reward bonus: <green>+"
						+ formatWholeMoney(caravanReward)));
			}

			double cargoTotal = 0;
			for (double value : psd.getSoldCargoValue().values()) cargoTotal += value;
			int caravanBonus = pd == null ? 0 : pd.getSellMultiplierBonus();
			double cargoMultiplier = b.notorietyMultiplier + caravanBonus / 100.0;
			double cargoReward = cargoTotal * cargoMultiplier * currencyMultiplier;
			if (cargoTotal > 0) {
				double effectiveCargoMultiplier = cargoMultiplier * currencyMultiplier;
				String multiplierText = isRelevantMultiplier(effectiveCargoMultiplier)
						? " (<white>\u00d7" + String.format("%.2f", effectiveCargoMultiplier) + "<gray>)"
						: "";
				lore.add(loreLine("<gray>Cargo sales" + multiplierText + ": <green>+"
						+ formatWholeMoney(cargoReward)));
			}
			double runTotal = finalReward + regionBaseReward + caravanReward + cargoReward;
			lore.add(Component.empty());
			lore.add(loreLine("<gold>Total run earnings: <yellow>" + formatWholeMoney(runTotal)));
		}
		return lore;
	}

	private static void addCurrencyBoostLore(List<Component> lore, PlayerSessionData psd) {
		if (!isRelevantMultiplier(psd.getRunCurrencyBoostMultiplier())) return;
		lore.add(loreLine("<gray>Currency boost: <green>\u00d7" + formatMult(psd.getRunCurrencyBoostMultiplier())));
		if (psd.getRunCurrencyBoosts().isEmpty()) return;
		for (PlayerSessionData.RunCurrencyBoost boost : psd.getRunCurrencyBoosts()) {
			lore.add(loreLine("<dark_gray>- <gray>" + boost.displayName() + ": <green>+"
					+ Math.round(boost.bonus() * 100) + "%"));
		}
	}

	// Builds the run-experience breakdown as item lore for the session summary inventory. Pass the
	// viewer's PlayerSessionData (null for spectators, who shouldn't see personal experience).
	public static List<Component> buildExpLore(Session s, PlayerSessionData psd) {
		List<Component> lore = new ArrayList<Component>();
		if (psd == null) return lore;
		if (!s.countsProgression()) {
			lore.add(loreLine("<gray>This run doesn't award experience."));
			return lore;
		}
		EquipmentClass ec = psd.getPlayerClass();
		int earned = psd.getSessionStats().getExpEarned();
		lore.add(loreLine("<gray>Class: <white>" + ec.getDisplay()));
		lore.add(loreLine("<gray>Total exp earned: <green>+" + earned));
		if (!psd.getRunExpBoosts().isEmpty()) {
			lore.add(Component.empty());
			lore.add(loreLine("<gold>Exp boosts applied:"));
			for (PlayerSessionData.RunExpBoost boost : psd.getRunExpBoosts()) {
				int percent = (int) Math.round(boost.bonus() * 100);
				lore.add(loreLine("<dark_gray>- <gray>" + boost.displayName() + ": <green>+" + percent + "%"));
			}
		}

		PlayerData pd = psd.getData();
		if (pd != null) {
			int level = pd.getLevel(ec);
			int exp = pd.getExp(ec);
			int req = PlayerData.getXpRequired(level);
			lore.add(loreLine("<gold>" + ec.getDisplay() + " level <yellow>" + level + " <gray>(<white>" + exp
					+ "<gray>/<white>" + req + "<gray>)"));
		}
		return lore;
	}

	// Deserializes a MiniMessage line into a non-italic lore Component.
	private static Component loreLine(String miniMessage) {
		return NeoCore.miniMessage().deserialize(miniMessage).decoration(TextDecoration.ITALIC, false);
	}

	private static String formatWholeMoney(double amount) {
		return String.valueOf(Math.round(amount));
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
		public final int partySize;
		public final double partyMultiplier;

		private Breakdown(boolean won, boolean zeroedByDeath, int nodesVisited, int regionsCompleted, int notoriety,
				double base, double nodeBonus, double regionBonus, double subtotal, double notorietyMultiplier,
				int partySize, double partyMultiplier, double total) {
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
			this.partySize = partySize;
			this.partyMultiplier = partyMultiplier;
			this.total = total;
		}
	}
}
