package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.player.boost.BoostDurationType;
import me.neoblade298.neorogue.player.boost.ExpBoostType;
import me.neoblade298.neorogue.player.boost.GlobalBoostManager;

public class CmdAdminGlobalBoost extends Subcommand {
	public CmdAdminGlobalBoost(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		ArrayList<String> typeTab = new ArrayList<String>();
		typeTab.add("clear");
		for (ExpBoostType type : ExpBoostType.values()) {
			if (type.getDurationType() == BoostDurationType.TIME) typeTab.add(type.name());
		}
		args.add(new Arg("type").setTabOptions(typeTab), new Arg("durationSeconds", false));
		this.enableTabComplete();
	}

	@Override
	public void run(CommandSender s, String[] args) {
		if (args[0].equalsIgnoreCase("clear")) {
			GlobalBoostManager.clear();
			Util.msgRaw(s, "<green>Cleared all global exp boosts");
			return;
		}

		ExpBoostType type;
		try {
			type = ExpBoostType.valueOf(args[0].toUpperCase());
		} catch (IllegalArgumentException ex) {
			Util.msgRaw(s, "<red>Unknown boost type! Options: " + typeList());
			return;
		}
		if (type.getDurationType() != BoostDurationType.TIME) {
			Util.msgRaw(s, "<red>Global boosts must be time-based! " + type.name() + " is run-based.");
			return;
		}

		if (args.length < 2) {
			Util.msgRaw(s, "<red>You must provide a duration in seconds!");
			return;
		}
		long duration;
		try {
			duration = Long.parseLong(args[1]);
		} catch (NumberFormatException ex) {
			Util.msgRaw(s, "<red>Duration must be a number!");
			return;
		}
		if (duration <= 0) {
			Util.msgRaw(s, "<red>Duration must be positive!");
			return;
		}

		GlobalBoostManager.addGlobalBoost(type, duration);
		Util.msgRaw(s, "<green>Activated global boost " + type.getDisplayName() + " (" + duration + "s)");
	}

	private String typeList() {
		StringBuilder sb = new StringBuilder();
		for (ExpBoostType type : ExpBoostType.values()) {
			if (type.getDurationType() != BoostDurationType.TIME) continue;
			if (sb.length() > 0) sb.append(", ");
			sb.append(type.name());
		}
		return sb.toString();
	}
}
