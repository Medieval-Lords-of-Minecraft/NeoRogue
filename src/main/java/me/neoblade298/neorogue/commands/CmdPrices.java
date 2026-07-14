package me.neoblade298.neorogue.commands;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.integration.DynamicPricingManager;
import me.neoblade298.neorogue.integration.MaterialPrices;

// Top-level /prices command: lists sellable materials with their base and current dynamic prices.
// Optionally filters by a name substring, e.g. /prices ingot.
public class CmdPrices extends Subcommand {
	private static final DecimalFormat DF = new DecimalFormat("#,##0.##");
	private static final DecimalFormat PCT = new DecimalFormat("0.#");
	private static final int MAX_ROWS = 60;

	public CmdPrices(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		this.enableTabComplete();
		args.add(new Arg("filter", false));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		String filter = args.length > 0 ? args[0].toLowerCase() : null;

		List<Material> mats = new ArrayList<Material>();
		for (Map.Entry<Material, Double> ent : MaterialPrices.getAll().entrySet()) {
			Material mat = ent.getKey();
			if (filter != null && !mat.name().toLowerCase().contains(filter)
					&& !prettyName(mat).toLowerCase().contains(filter)) {
				continue;
			}
			mats.add(mat);
		}

		if (mats.isEmpty()) {
			Util.msgRaw(s, "<red>No sellable materials" + (filter != null ? " matched '" + filter + "'." : "."));
			return;
		}

		// Sort by current price descending so the most valuable items show first.
		mats.sort(Comparator.comparingDouble((Material m) -> MaterialPrices.getPrice(m)).reversed());

		Util.msgRaw(s, "<gray>Sell prices" + (filter != null ? " matching <yellow>" + filter : "")
				+ " <gray>(<yellow>" + mats.size() + "<gray> found):");
		int shown = 0;
		for (Material mat : mats) {
			if (shown >= MAX_ROWS) {
				Util.msgRaw(s, "<gray>...and <yellow>" + (mats.size() - shown)
						+ " <gray>more. Narrow with <yellow>/prices <filter>");
				break;
			}
			double current = MaterialPrices.getPrice(mat);
			Util.msgRaw(s, "<gray>- <white>" + prettyName(mat) + "<gray>: <gold>" + DF.format(current) + trendSuffix(mat));
			shown++;
		}
	}

	// Converts an enum material name (e.g. IRON_INGOT) to a readable label (e.g. Iron Ingot).
	static String prettyName(Material mat) {
		String[] parts = mat.name().toLowerCase().split("_");
		StringBuilder sb = new StringBuilder();
		for (String part : parts) {
			if (part.isEmpty()) continue;
			if (sb.length() > 0) sb.append(' ');
			sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
		}
		return sb.toString();
	}

	// A colored suffix showing how far the current dynamic price sits above/below base, e.g. " (+12%)".
	// Returns an empty string when the price is at its base value.
	static String trendSuffix(Material mat) {
		double mult = DynamicPricingManager.getMultiplier(mat);
		double pct = (mult - 1) * 100;
		if (Math.abs(pct) < 0.05) return "";
		if (pct > 0) return " <green>(+" + PCT.format(pct) + "%)";
		return " <red>(" + PCT.format(pct) + "%)";
	}
}
