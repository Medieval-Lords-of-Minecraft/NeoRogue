package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.player.boost.BoostDurationType;
import me.neoblade298.neorogue.player.boost.BoostTimeFormat;
import me.neoblade298.neorogue.player.boost.CurrencyBoostType;
import me.neoblade298.neorogue.player.boost.GlobalCurrencyBoostManager;

public class CmdAdminGlobalCurrencyBoost extends Subcommand {
	public CmdAdminGlobalCurrencyBoost(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		ArrayList<String> typeTab = new ArrayList<String>();
		typeTab.add("clear");
		for (CurrencyBoostType type : CurrencyBoostType.values()) {
			if (type.getDurationType() == BoostDurationType.TIME) typeTab.add(type.name());
		}
		args.add(new Arg("type").setTabOptions(typeTab), new Arg("duration", false), new Arg("player", false));
		this.enableTabComplete();
	}

	@Override
	public void run(CommandSender s, String[] args) {
		if (args[0].equalsIgnoreCase("clear")) {
			GlobalCurrencyBoostManager.clear();
			Util.msgRaw(s, "<green>Cleared all global currency boosts");
			return;
		}

		CurrencyBoostType type;
		try {
			type = CurrencyBoostType.valueOf(args[0].toUpperCase());
		} catch (IllegalArgumentException ex) {
			Util.msgRaw(s, "<red>Unknown currency boost type! Options: " + typeList());
			return;
		}
		if (type.getDurationType() != BoostDurationType.TIME) {
			Util.msgRaw(s, "<red>Global currency boosts must use the TIME duration type.");
			return;
		}
		if (args.length < 2) {
			Util.msgRaw(s, "<red>You must provide a duration in seconds!");
			return;
		}

		long duration;
		try {
			duration = BoostTimeFormat.parseSeconds(args[1]);
		} catch (ArithmeticException | NumberFormatException ex) {
			Util.msgRaw(s, "<red>Duration must be a number of seconds or formatted time "
					+ "(for example, 30m, 1d, or 1d12h)!");
			return;
		}
		if (duration <= 0) {
			Util.msgRaw(s, "<red>Duration must be positive!");
			return;
		}

		boolean extended = GlobalCurrencyBoostManager.addGlobalBoost(type, duration);
		String playerName = args.length > 2 ? args[2] : s.getName();
		long remaining = GlobalCurrencyBoostManager.getGlobalBoostRemaining(type);
		String message = type.formatGrantMessage(extended, playerName, duration, remaining);
		if (message != null) {
			for (Player player : Bukkit.getOnlinePlayers()) Util.msgRaw(player, message);
		}
		Util.msgRaw(s, "<green>" + (extended ? "Extended" : "Activated") + " global currency boost "
				+ type.getDisplayName() + " (" + BoostTimeFormat.format(duration) + ")");
	}

	private String typeList() {
		StringBuilder sb = new StringBuilder();
		for (CurrencyBoostType type : CurrencyBoostType.values()) {
			if (type.getDurationType() != BoostDurationType.TIME) continue;
			if (sb.length() > 0) sb.append(", ");
			sb.append(type.name());
		}
		return sb.toString();
	}
}
