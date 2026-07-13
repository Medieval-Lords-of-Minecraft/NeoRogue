package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.session.analytics.AnalyticsManager;

public class CmdLyticsEquipment extends Subcommand {
	public CmdLyticsEquipment(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		enableTabComplete();
		args.add(new Arg("equipment", true));
		ArrayList<String> versions = new ArrayList<String>();
		versions.add(String.valueOf(AnalyticsManager.BALANCE_VERSION));
		args.add(new Arg("balanceVersion", false).setTabOptions(versions));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		if (args.length < 1) {
			Util.msgRaw(s, "<red>Usage: /nrlytics equipment <equipment> [balanceVersion]");
			return;
		}
		int version = AnalyticsManager.BALANCE_VERSION;
		if (args.length > 1) {
			try {
				version = Integer.parseInt(args[1]);
			}
			catch (NumberFormatException ex) {
				Util.msgRaw(s, "<red>Balance version must be a number.");
				return;
			}
		}
		AnalyticsReport.equipment(s, args[0], version);
	}
}
