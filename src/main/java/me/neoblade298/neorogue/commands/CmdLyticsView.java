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
		enableTabComplete();
		args.add(new Arg("view").setTabOptions(new ArrayList<String>(VIEWS)));

		// Suggest the available filter keys for the trailing filter tokens.
		ArrayList<String> filterKeys = new ArrayList<String>();
		for (AnalyticsFilters.FilterOption o : AnalyticsReport.EQUIPMENT_FILTER_OPTIONS) {
			filterKeys.add(o.key + "=");
		}
		args.add(new Arg("filters...", false).setTabOptions(filterKeys));
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
