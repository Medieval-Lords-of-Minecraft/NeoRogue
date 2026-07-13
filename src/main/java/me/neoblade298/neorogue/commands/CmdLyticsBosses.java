package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.session.analytics.AnalyticsManager;

public class CmdLyticsBosses extends Subcommand {
	public CmdLyticsBosses(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		enableTabComplete();
		args.add(new Arg("class", false).setTabOptions(playerClasses()));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		String playerClass = args.length > 0 ? args[0].toUpperCase() : null;
		AnalyticsReport.bosses(s, AnalyticsManager.getQueryBalanceVersion(), playerClass);
	}

	// The player-selectable classes, used as the tab options for class filters across lytics commands.
	public static ArrayList<String> playerClasses() {
		ArrayList<String> classes = new ArrayList<String>();
		for (EquipmentClass ec : EquipmentClass.values()) {
			if (ec == EquipmentClass.SHOP || ec == EquipmentClass.CLASSLESS) continue;
			classes.add(ec.name());
		}
		return classes;
	}
}
