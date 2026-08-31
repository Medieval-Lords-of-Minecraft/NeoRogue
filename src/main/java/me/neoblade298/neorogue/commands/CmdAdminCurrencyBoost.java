package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.boost.BoostDurationType;
import me.neoblade298.neorogue.player.boost.BoostTimeFormat;
import me.neoblade298.neorogue.player.boost.CurrencyBoostType;

public class CmdAdminCurrencyBoost extends Subcommand {
	public CmdAdminCurrencyBoost(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		ArrayList<String> typeTab = new ArrayList<String>();
		typeTab.add("reset");
		for (CurrencyBoostType type : CurrencyBoostType.values()) {
			if (type.isGrantable()) typeTab.add(type.name());
		}
		args.add(new Arg("type").setTabOptions(typeTab), new Arg("duration", false), new Arg("player", false));
		this.enableTabComplete();
	}

	@Override
	public void run(CommandSender s, String[] args) {
		if (args[0].equalsIgnoreCase("reset")) {
			Player target = args.length > 1 ? Bukkit.getPlayer(args[1]) : (s instanceof Player ? (Player) s : null);
			if (target == null) {
				Util.msgRaw(s, "<red>That player isn't online!");
				return;
			}
			PlayerData data = PlayerManager.getPlayerData(target.getUniqueId());
			if (data == null) {
				Util.msgRaw(s, "<red>That player has no data!");
				return;
			}
			data.clearCurrencyBoosts();
			Util.msgRaw(s, "<green>Cleared all currency boosts from " + target.getName());
			return;
		}

		CurrencyBoostType type;
		try {
			type = CurrencyBoostType.valueOf(args[0].toUpperCase());
		} catch (IllegalArgumentException ex) {
			Util.msgRaw(s, "<red>Unknown currency boost type! Options: " + typeList());
			return;
		}
		if (!type.isGrantable()) {
			Util.msgRaw(s, "<red>That boost is granted by permission and has no duration.");
			return;
		}
		if (args.length < 2) {
			Util.msgRaw(s, "<red>You must provide a duration!");
			return;
		}

		long duration;
		try {
			duration = type.getDurationType() == BoostDurationType.TIME
					? BoostTimeFormat.parseSeconds(args[1]) : Long.parseLong(args[1]);
		} catch (ArithmeticException | NumberFormatException ex) {
			String format = type.getDurationType() == BoostDurationType.TIME
					? "a number of seconds or formatted time (for example, 30m, 1d, or 1d12h)"
					: "a number of runs";
			Util.msgRaw(s, "<red>Duration must be " + format + "!");
			return;
		}
		if (duration <= 0) {
			Util.msgRaw(s, "<red>Duration must be positive!");
			return;
		}

		Player target = args.length > 2 ? Bukkit.getPlayer(args[2]) : (s instanceof Player ? (Player) s : null);
		if (target == null) {
			Util.msgRaw(s, "<red>That player isn't online!");
			return;
		}
		PlayerData data = PlayerManager.getPlayerData(target.getUniqueId());
		if (data == null) {
			Util.msgRaw(s, "<red>That player has no data!");
			return;
		}

		boolean extended = data.addCurrencyBoost(type, duration);
		long remaining = data.getCurrencyBoostRemaining(type);
		String message = type.formatGrantMessage(extended, target.getName(), duration, remaining);
		if (message != null) Util.msgRaw(target, message);
		String unit = type.getDurationType() == BoostDurationType.TIME
				? BoostTimeFormat.format(duration) : duration + " run(s)";
		Util.msgRaw(s, "<green>" + (extended ? "Extended " : "Granted ") + type.getDisplayName()
				+ " (" + unit + ") " + (extended ? "for " : "to ") + target.getName());
	}

	private String typeList() {
		StringBuilder sb = new StringBuilder();
		for (CurrencyBoostType type : CurrencyBoostType.values()) {
			if (!type.isGrantable()) continue;
			if (sb.length() > 0) sb.append(", ");
			sb.append(type.name());
		}
		return sb.toString();
	}
}
