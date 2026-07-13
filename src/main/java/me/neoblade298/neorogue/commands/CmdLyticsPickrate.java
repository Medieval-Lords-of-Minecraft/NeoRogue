package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.session.analytics.AnalyticsManager;
import me.neoblade298.neorogue.session.analytics.OfferSnapshot.OfferSource;

public class CmdLyticsPickrate extends Subcommand {
	public CmdLyticsPickrate(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		enableTabComplete();
		ArrayList<String> sources = new ArrayList<String>();
		for (OfferSource src : OfferSource.values()) sources.add(src.name());
		args.add(new Arg("source", false).setTabOptions(sources));
		ArrayList<String> classes = new ArrayList<String>();
		for (EquipmentClass ec : EquipmentClass.values()) classes.add(ec.name());
		args.add(new Arg("class", false).setTabOptions(classes));
		ArrayList<String> sorts = new ArrayList<String>();
		sorts.add("rate");
		sorts.add("class");
		args.add(new Arg("sortBy", false).setTabOptions(sorts));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		String source = args.length > 0 ? args[0].toUpperCase() : null;
		String eqClass = args.length > 1 ? args[1].toUpperCase() : null;
		String sortBy = args.length > 2 ? args[2].toLowerCase() : "rate";
		AnalyticsReport.pickrate(s, AnalyticsManager.getQueryBalanceVersion(), source, eqClass, sortBy);
	}
}
