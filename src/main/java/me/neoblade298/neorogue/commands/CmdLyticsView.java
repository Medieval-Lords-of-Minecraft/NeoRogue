package me.neoblade298.neorogue.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.session.analytics.AnalyticsManager;

// Entry point for filterable analytics views: /nrlytics view <view> [key=value ...]. Each view maps
// to a report method; the trailing key=value tokens are parsed by the shared AnalyticsFilters helper
// so every view filters in the same consistent way.
public class CmdLyticsView extends Subcommand {
	private static final List<String> VIEWS = List.of("equipment");

	public CmdLyticsView(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		this.overrideTabHandler();
		args.add(new Arg("view").setTabOptions(new ArrayList<String>(VIEWS)));

		// Suggest the available filter keys for the trailing filter tokens.
		ArrayList<String> filterKeys = new ArrayList<String>();
		for (AnalyticsFilters.FilterOption o : AnalyticsReport.EQUIPMENT_FILTER_OPTIONS) {
			filterKeys.add(o.key + "=");
		}
		args.add(new Arg("filters...", false).setTabOptions(filterKeys));
		args.setMax(-1);
	}

	// Context-aware completion: the view name first, then filter keys, then a key's allowed values
	// once the user has typed "key=". The framework prefix-filters whatever list we return.
	@Override
	public List<String> getTabOptions(CommandSender s, String[] args) {
		// Completing the view name
		if (args.length <= 1) return new ArrayList<String>(VIEWS);

		// Remaining tokens are key=value filters; which keys are valid depends on the chosen view.
		List<AnalyticsFilters.FilterOption> options = filterOptionsFor(args[0]);
		if (options == null) return null;

		String token = args[args.length - 1];
		int eq = token.indexOf('=');
		ArrayList<String> out = new ArrayList<String>();

		// No '=' yet: suggest the available filter keys.
		if (eq < 0) {
			for (AnalyticsFilters.FilterOption o : options) out.add(o.key + "=");
			return out;
		}

		// "key=" typed: suggest that key's allowed values (null = free-form, no suggestions).
		String key = token.substring(0, eq).toLowerCase();
		for (AnalyticsFilters.FilterOption o : options) {
			if (!o.key.equalsIgnoreCase(key)) continue;
			if (o.allowed == null) return null;
			for (String value : o.allowed) out.add(o.key + "=" + value);
			return out;
		}
		return null;
	}

	// Maps a view name to its filter options (currently only the equipment view is filterable).
	private static List<AnalyticsFilters.FilterOption> filterOptionsFor(String view) {
		if (view.equalsIgnoreCase("equipment")) return AnalyticsReport.EQUIPMENT_FILTER_OPTIONS;
		return null;
	}

	@Override
	public void run(CommandSender s, String[] args) {
		if (args.length < 1) {
			Util.msgRaw(s, "<red>Usage: /nrlytics view <view> [key=value ...]");
			Util.msgRaw(s, "<gray>Views: <white>" + String.join(", ", VIEWS));
			return;
		}

		String view = args[0].toLowerCase();
		int version = AnalyticsManager.getQueryBalanceVersion();
		switch (view) {
		case "equipment":
			AnalyticsFilters filters = AnalyticsFilters.parse(args, 1, AnalyticsReport.EQUIPMENT_FILTER_OPTIONS);
			AnalyticsReport.equipmentDamage(s, version, filters);
			break;
		default:
			Util.msgRaw(s, "<red>Unknown view '" + view + "'. Available: " + String.join(", ", VIEWS));
		}
	}
}
