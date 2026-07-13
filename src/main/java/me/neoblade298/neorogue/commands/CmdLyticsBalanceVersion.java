package me.neoblade298.neorogue.commands;

import org.bukkit.command.CommandSender;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.session.analytics.AnalyticsManager;

public class CmdLyticsBalanceVersion extends Subcommand {
	public CmdLyticsBalanceVersion(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		enableTabComplete();
		args.add(new Arg("version", false));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		if (args.length < 1) {
			Util.msgRaw(s, "<gray>Analytics balance version: <yellow>" + AnalyticsManager.getQueryBalanceVersion()
					+ "</yellow> <gray>(latest: <yellow>" + AnalyticsManager.BALANCE_VERSION + "</yellow><gray>)");
			return;
		}
		int version;
		try {
			version = Integer.parseInt(args[0]);
		}
		catch (NumberFormatException ex) {
			Util.msgRaw(s, "<red>Balance version must be a number.");
			return;
		}
		AnalyticsManager.setQueryBalanceVersion(version);
		Util.msgRaw(s, "<gray>Analytics balance version set to <yellow>" + version);
	}
}
