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
import me.neoblade298.neorogue.player.boost.ExpBoostType;

public class CmdAdminBoost extends Subcommand {
	public CmdAdminBoost(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		ArrayList<String> typeTab = new ArrayList<String>();
		for (ExpBoostType type : ExpBoostType.values()) {
			typeTab.add(type.name());
		}
		args.add(new Arg("type").setTabOptions(typeTab), new Arg("duration"), new Arg("player", false));
		this.enableTabComplete();
	}

	@Override
	public void run(CommandSender s, String[] args) {
		ExpBoostType type;
		try {
			type = ExpBoostType.valueOf(args[0].toUpperCase());
		} catch (IllegalArgumentException ex) {
			Util.msgRaw(s, "<red>Unknown boost type! Options: " + typeList());
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

		Player p = args.length > 2 ? Bukkit.getPlayer(args[2]) : (s instanceof Player ? (Player) s : null);
		if (p == null) {
			Util.msgRaw(s, "<red>That player isn't online!");
			return;
		}
		PlayerData pdata = PlayerManager.getPlayerData(p.getUniqueId());
		if (pdata == null) {
			Util.msgRaw(s, "<red>That player has no data!");
			return;
		}

		pdata.addExpBoost(type, duration);
		String unit = type.getDurationType() == BoostDurationType.TIME ? duration + "s" : duration + " run(s)";
		Util.msgRaw(s, "<green>Granted " + type.getDisplayName() + " (" + unit + ") to " + p.getName());
	}

	private String typeList() {
		StringBuilder sb = new StringBuilder();
		for (ExpBoostType type : ExpBoostType.values()) {
			if (sb.length() > 0) sb.append(", ");
			sb.append(type.name());
		}
		return sb.toString();
	}
}
