package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.analytics.AnalyticsManager;

public class CmdLyticsMobs extends Subcommand {
	public CmdLyticsMobs(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		enableTabComplete();
		ArrayList<String> regions = new ArrayList<String>();
		for (RegionType rt : RegionType.values()) {
			if (rt.name().contains("DEBUG")) continue;
			regions.add(rt.name());
		}
		args.add(new Arg("regionType", false).setTabOptions(regions));
		args.add(new Arg("class", false).setTabOptions(CmdLyticsBosses.playerClasses()));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		String regionType = args.length > 0 ? args[0].toUpperCase() : null;
		String playerClass = args.length > 1 ? args[1].toUpperCase() : null;
		AnalyticsReport.mobs(s, AnalyticsManager.getQueryBalanceVersion(), regionType, playerClass);
	}
}
