package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.analytics.AnalyticsManager;

public class CmdLyticsMobs extends Subcommand {
	public CmdLyticsMobs(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		enableTabComplete();
		ArrayList<String> versions = new ArrayList<String>();
		versions.add(String.valueOf(AnalyticsManager.BALANCE_VERSION));
		args.add(new Arg("balanceVersion", false).setTabOptions(versions));
		ArrayList<String> regions = new ArrayList<String>();
		for (RegionType rt : RegionType.values()) {
			if (rt.name().contains("DEBUG")) continue;
			regions.add(rt.name());
		}
		args.add(new Arg("regionType", false).setTabOptions(regions));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		int version = AnalyticsManager.BALANCE_VERSION;
		if (args.length > 0) {
			try {
				version = Integer.parseInt(args[0]);
			}
			catch (NumberFormatException ex) {
				Util.msgRaw(s, "<red>Balance version must be a number.");
				return;
			}
		}
		String regionType = args.length > 1 ? args[1].toUpperCase() : null;
		AnalyticsReport.mobs(s, version, regionType);
	}
}
