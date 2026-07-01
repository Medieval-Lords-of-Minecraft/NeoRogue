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
import me.neoblade298.neorogue.player.unlock.UnlockRegistry;

public class CmdAdminUnlock extends Subcommand {
	private static enum Action {
		GRANT, REVOKE;
	}

	public CmdAdminUnlock(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		ArrayList<String> actions = new ArrayList<String>();
		for (Action action : Action.values()) {
			actions.add(action.name().toLowerCase());
		}
		ArrayList<String> nodeOptions = UnlockRegistry.getSortedNodeIds();
		nodeOptions.add(0, "all");
		args.add(new Arg("action").setTabOptions(actions),
				new Arg("node").setTabOptions(nodeOptions),
				new Arg("player", false));
		this.enableTabComplete();
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Action action;
		try {
			action = Action.valueOf(args[0].toUpperCase());
		}
		catch (IllegalArgumentException e) {
			Util.msgRaw(s, "<red>Invalid action. Use grant/revoke.");
			return;
		}

		if (args.length < 2) {
			Util.msgRaw(s, "<red>Usage: /nradmin unlock " + action.name().toLowerCase() + " <node> [player]");
			return;
		}

		Player target;
		if (args.length > 2) {
			target = Bukkit.getPlayer(args[2]);
		}
		else if (s instanceof Player) {
			target = (Player) s;
		}
		else {
			Util.msgRaw(s, "<red>You must provide a player from console.");
			return;
		}

		if (target == null) {
			Util.msgRaw(s, "<red>That player is not online.");
			return;
		}

		PlayerData data = PlayerManager.getPlayerData(target.getUniqueId());
		if (data == null) {
			Util.msgRaw(s, "<red>No loaded player data found for " + target.getName() + ".");
			return;
		}

		if (args[1].equalsIgnoreCase("all")) {
			int count = 0;
			for (String id : UnlockRegistry.getNodeIds()) {
				boolean changed = action == Action.GRANT ? data.grant(id) : data.revoke(id);
				if (changed) count++;
			}
			String verb = action == Action.GRANT ? "Granted" : "Revoked";
			Util.msgRaw(s, "<green>" + verb + " <yellow>" + count + "</yellow> unlock nodes for " + target.getName() + ".");
			return;
		}

		String nodeId = UnlockRegistry.normalizeNodeId(args[1]);
		if (!UnlockRegistry.hasNode(nodeId)) {
			Util.msgRaw(s, "<red>Unknown unlock node: " + args[1]);
			return;
		}

		boolean changed = action == Action.GRANT ? data.grant(nodeId) : data.revoke(nodeId);
		if (!changed) {
			Util.msgRaw(s, "<yellow>No changes made.");
			return;
		}
		String verb = action == Action.GRANT ? "Granted" : "Revoked";
		Util.msgRaw(s, "<green>" + verb + " unlock node <yellow>" + nodeId + "</yellow> for " + target.getName() + ".");
	}
}
