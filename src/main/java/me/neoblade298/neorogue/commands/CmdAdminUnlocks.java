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

public class CmdAdminUnlocks extends Subcommand {

	public CmdAdminUnlocks(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("player", false));
		this.enableTabComplete();
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player target;
		if (args.length > 0) {
			target = Bukkit.getPlayer(args[0]);
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

		Set<String> unlockNodes = data.getUnlockNodes();
		if (unlockNodes.isEmpty()) {
			Util.msgRaw(s, "<yellow>" + target.getName() + " has no unlock nodes.");
			return;
		}
		ArrayList<String> list = new ArrayList<String>(unlockNodes);
		Collections.sort(list);
		Util.msgRaw(s, "<yellow>" + target.getName() + " unlock nodes:</yellow> " + String.join(", ", list));
	}
}
