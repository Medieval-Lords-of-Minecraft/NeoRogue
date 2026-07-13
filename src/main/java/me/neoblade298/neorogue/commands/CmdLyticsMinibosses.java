package me.neoblade298.neorogue.commands;

import org.bukkit.command.CommandSender;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.session.analytics.AnalyticsManager;

public class CmdLyticsMinibosses extends Subcommand {
	public CmdLyticsMinibosses(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		enableTabComplete();
		args.add(new Arg("class", false).setTabOptions(CmdLyticsBosses.playerClasses()));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		String playerClass = args.length > 0 ? args[0].toUpperCase() : null;
		AnalyticsReport.minibosses(s, AnalyticsManager.getQueryBalanceVersion(), playerClass);
	}
}
