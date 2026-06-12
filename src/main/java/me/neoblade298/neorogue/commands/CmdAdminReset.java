package me.neoblade298.neorogue.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;

public class CmdAdminReset extends Subcommand {

	public CmdAdminReset(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("player"));
		this.enableTabComplete();
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player target = Bukkit.getPlayer(args[0]);
		if (target == null) {
			Util.msg(s, "<red>That player is not online.");
			return;
		}

		PlayerData data = PlayerManager.getPlayerData(target.getUniqueId());
		if (data == null) {
			Util.msg(s, "<red>No loaded player data found for " + target.getName() + ".");
			return;
		}

		data.resetAll();
		Util.msg(s, "<green>Reset all progress for <yellow>" + target.getName() + "</yellow>.");
	}
}
