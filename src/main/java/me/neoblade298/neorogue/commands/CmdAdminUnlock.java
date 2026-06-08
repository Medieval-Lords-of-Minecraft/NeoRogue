package me.neoblade298.neorogue.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.unlock.UnlockRegistry;

public class CmdAdminUnlock extends Subcommand {
	private static enum Action {
		GRANT, REVOKE, LIST, NODES;
	}

	public CmdAdminUnlock(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		ArrayList<String> actions = new ArrayList<String>();
		for (Action action : Action.values()) {
			actions.add(action.name().toLowerCase());
		}
		args.add(new Arg("action").setTabOptions(actions), new Arg("player", false),
				new Arg("node", false).setTabOptions(UnlockRegistry.getSortedNodeIds()));
		this.enableTabComplete();
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Action action;
		try {
			action = Action.valueOf(args[0].toUpperCase());
		}
		catch (IllegalArgumentException e) {
			Util.msg(s, "<red>Invalid action. Use grant/revoke/list/nodes.");
			return;
		}

		if (action == Action.NODES) {
			ArrayList<String> nodes = UnlockRegistry.getSortedNodeIds();
			if (nodes.isEmpty()) {
				Util.msg(s, "<yellow>No unlock nodes are currently registered.");
				return;
			}
			Util.msg(s, "<yellow>Registered unlock nodes:</yellow> " + String.join(", ", nodes));
			return;
		}

		Player target;
		if (args.length > 1) {
			target = Bukkit.getPlayer(args[1]);
		}
		else if (s instanceof Player) {
			target = (Player) s;
		}
		else {
			Util.msg(s, "<red>You must provide a player from console.");
			return;
		}

		if (target == null) {
			Util.msg(s, "<red>That player is not online.");
			return;
		}

		PlayerData data = PlayerManager.getPlayerData(target.getUniqueId());
		if (data == null) {
			Util.msg(s, "<red>No loaded player data found for " + target.getName() + ".");
			return;
		}

		switch (action) {
		case LIST:
			Set<String> unlockNodes = data.getUnlockNodes();
			if (unlockNodes.isEmpty()) {
				Util.msg(s, "<yellow>" + target.getName() + " has no unlock nodes.");
				return;
			}
			ArrayList<String> list = new ArrayList<String>(unlockNodes);
			Collections.sort(list);
			Util.msg(s, "<yellow>" + target.getName() + " unlock nodes:</yellow> " + String.join(", ", list));
			return;
		case GRANT:
		case REVOKE:
			if (args.length < 3) {
				Util.msg(s, "<red>Usage: /nradmin unlock " + action.name().toLowerCase() + " <player> <node>");
				return;
			}
			String nodeId = UnlockRegistry.normalizeNodeId(args[2]);
			if (!UnlockRegistry.hasNode(nodeId)) {
				Util.msg(s, "<red>Unknown unlock node: " + args[2]);
				return;
			}

			boolean changed = action == Action.GRANT ? data.grant(nodeId) : data.revoke(nodeId);
			if (!changed) {
				Util.msg(s, "<yellow>No changes made.");
				return;
			}
			String verb = action == Action.GRANT ? "Granted" : "Revoked";
			Util.msg(s, "<green>" + verb + " unlock node <yellow>" + nodeId + "</yellow> for " + target.getName() + ".");
			return;
		case NODES:
		default:
			return;
		}
	}
}
