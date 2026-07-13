package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.session.analytics.AnalyticsManager;
import me.neoblade298.neorogue.session.chance.ChanceSet;

public class CmdLyticsChance extends Subcommand {
	public CmdLyticsChance(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		enableTabComplete();
		ArrayList<String> versions = new ArrayList<String>();
		versions.add(String.valueOf(AnalyticsManager.BALANCE_VERSION));
		args.add(new Arg("balanceVersion", false).setTabOptions(versions));
		args.add(new Arg("setId", false).setTabOptions(new ArrayList<String>(ChanceSet.getSets())));
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
		String setId = args.length > 1 ? args[1] : null;
		AnalyticsReport.chance(s, version, setId);
	}
}
