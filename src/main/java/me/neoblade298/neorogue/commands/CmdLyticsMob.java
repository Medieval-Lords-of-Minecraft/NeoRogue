package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.session.analytics.AnalyticsManager;
import me.neoblade298.neorogue.session.fight.Mob;

public class CmdLyticsMob extends Subcommand {
	public CmdLyticsMob(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		enableTabComplete();
		args.add(new Arg("mobId", true).setTabOptions(new ArrayList<String>(Mob.getStatIds())));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		if (args.length < 1) {
			Util.msgRaw(s, "<red>Usage: /nrlytics mob <mobId>");
			return;
		}
		AnalyticsReport.mob(s, args[0], AnalyticsManager.getQueryBalanceVersion());
	}
}
