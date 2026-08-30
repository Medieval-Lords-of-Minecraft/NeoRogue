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
import me.neoblade298.neorogue.player.inventory.MainMenuInventory;
import me.neoblade298.neorogue.player.inventory.TutorialPromptDialog;

public class CmdAdminOpenMenu extends Subcommand {
	public CmdAdminOpenMenu(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("menu").setTabOptions(new ArrayList<>(List.of("main", "tutorial"))), new Arg("player"));
		enableTabComplete();
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (args.length < 2) {
			Util.msgRaw(sender, "<red>Usage: /nradmin openmenu <main|tutorial> <player>");
			return;
		}

		Player target = Bukkit.getPlayerExact(args[1]);
		if (target == null) {
			Util.msgRaw(sender, "<red>That player is not online.");
			return;
		}

		switch (args[0].toLowerCase(java.util.Locale.ROOT)) {
		case "main":
			new MainMenuInventory(target);
			Util.msgRaw(sender, "<green>Opened the main menu for " + target.getName() + ".");
			break;
		case "tutorial":
			TutorialPromptDialog.show(target);
			Util.msgRaw(sender, "<green>Opened the tutorial menu for " + target.getName() + ".");
			break;
		default:
			Util.msgRaw(sender, "<red>Unknown menu: " + args[0] + ". Available menus: main, tutorial");
		}
	}
}