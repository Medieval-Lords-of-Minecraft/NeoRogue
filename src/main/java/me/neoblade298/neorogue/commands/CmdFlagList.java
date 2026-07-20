package me.neoblade298.neorogue.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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

public class CmdFlagList extends Subcommand {
	public CmdFlagList(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("player", false), new Arg("namespace", false));
		this.overrideTabHandler();
	}

	@Override
	public List<String> getTabOptions(CommandSender s, String[] args) {
		// args[0] is the subcommand key ("list"); args[1] = player (or namespace shorthand), args[2] = namespace.
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
		// Accept "[player] [namespace]"; also accept a lone namespace ("/nrflag list caravan") as a
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
			List<String> flags = pdata.getFlags(namespace);
			if (flags.isEmpty()) {
				Util.msgRaw(s, "<yellow>" + target.getName() + " has no flags in namespace '<white>" + namespace + "<yellow>'.");
				return;
			}
			Util.msgRaw(s, "<yellow>" + target.getName() + " flags [<white>" + namespace + "<yellow>]:</yellow> " + String.join(", ", flags));
			return;
		}

		Set<String> all = pdata.getFlags();
		if (all.isEmpty()) {
			Util.msgRaw(s, "<yellow>" + target.getName() + " has no flags.");
			return;
		}
		// Group by namespace for readability; flags with no namespace fall under "(none)".
		TreeMap<String, TreeSet<String>> grouped = new TreeMap<String, TreeSet<String>>();
		for (String flag : all) {
			String ns = FlagRegistry.namespaceOf(flag);
			if (ns.isEmpty()) ns = "(none)";
			grouped.computeIfAbsent(ns, k -> new TreeSet<String>()).add(flag);
		}
		Util.msgRaw(s, "<yellow>" + target.getName() + " flags (" + all.size() + "):");
		for (var entry : grouped.entrySet()) {
			Util.msgRaw(s, "<gold>" + entry.getKey() + ":</gold> <white>" + String.join(", ", entry.getValue()));
		}
	}
}
