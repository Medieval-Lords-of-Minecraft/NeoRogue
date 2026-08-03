package me.neoblade298.neorogue.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.inventory.MainSessionMenu;

public class CmdProfile extends Subcommand {
	public CmdProfile(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("player"));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player viewer = (Player) s;
		OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
		PlayerData targetData = PlayerManager.getOrLoadPlayerData(target);
		if (targetData == null) {
			Util.displayError(viewer, "That player has never joined the server!");
			return;
		}
		new MainSessionMenu(viewer, targetData);
	}
}