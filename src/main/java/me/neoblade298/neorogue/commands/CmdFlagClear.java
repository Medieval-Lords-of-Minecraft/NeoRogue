package me.neoblade298.neorogue.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.player.FlagRegistry;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;

public class CmdFlagClear extends Subcommand {
	public CmdFlagClear(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("player", false), new Arg("namespace", false));
		this.overrideTabHandler();
	}

	@Override
	public List<String> getTabOptions(CommandSender s, String[] args) {
		// args[0] is the subcommand key ("clear"); args[1] = player (or namespace shorthand), args[2] = namespace.
		if (args.length == 2) {
			ArrayList<String> options = new ArrayList<String>(FlagCommandUtil.onlinePlayerNames(args[1]));
			options.addAll(FlagCommandUtil.filter(FlagRegistry.getNamespaces(), args[1]));
			return options;
		}
		if (args.length == 3) return FlagCommandUtil.filter(FlagRegistry.getNamespaces(), args[2]);
		return List.of();
	}

	@Override
	public void run(CommandSender s, String[] args) {
		// Accept "[player] [namespace]"; also accept a lone namespace ("/nrflag clear caravan") as a
		// filter on the sender when it names a registered namespace and no such player is online.
		String playerName = null, namespace = null;
		if (args.length >= 2) {
			playerName = args[0];
			namespace = args[1];
		}
		else if (args.length == 1) {
			if (FlagRegistry.hasNamespace(args[0]) && Bukkit.getPlayer(args[0]) == null) namespace = args[0];
			else playerName = args[0];
		}

		Player target = FlagCommandUtil.resolveTarget(s, playerName);
		if (target == null) {
			Util.msgRaw(s, "<red>That player isn't online! (or provide a player from console)");
			return;
		}

		PlayerData pdata = PlayerManager.getPlayerData(target.getUniqueId());
		if (pdata == null) {
			Util.msgRaw(s, "<red>No loaded player data found for " + target.getName() + ".");
			return;
		}

		if (namespace != null) {
			int removed = pdata.clearFlags(namespace);
			Util.msgRaw(s, "<green>Cleared <white>" + removed + "<green> flag(s) in namespace '<white>" + namespace + "<green>' from " + target.getName() + ".");
			return;
		}

		int removed = pdata.clearFlags();
		Util.msgRaw(s, "<green>Cleared all <white>" + removed + "<green> flag(s) from " + target.getName() + ".");
	}
}
