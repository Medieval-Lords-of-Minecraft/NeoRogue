package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.session.analytics.AnalyticsManager;
import me.neoblade298.neorogue.session.chance.ChanceSet;

public class CmdLyticsChance extends Subcommand {
	public CmdLyticsChance(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		enableTabComplete();
		args.add(new Arg("setId", false).setTabOptions(new ArrayList<String>(ChanceSet.getSets())));
		args.add(new Arg("class", false).setTabOptions(CmdLyticsBosses.playerClasses()));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		String setId = args.length > 0 ? args[0] : null;
		String playerClass = args.length > 1 ? args[1].toUpperCase() : null;
		AnalyticsReport.chance(s, AnalyticsManager.getQueryBalanceVersion(), setId, playerClass);
	}
}
