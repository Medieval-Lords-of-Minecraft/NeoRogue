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
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.tutorial.TutorialManager;

public class CmdAdminFlag extends Subcommand {
	public CmdAdminFlag(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("add/remove").setTabOptions(new ArrayList<>(List.of("add", "remove"))),
				new Arg("flag").setTabOptions(new ArrayList<>(TutorialManager.getAllFlags())),
				new Arg("player", false));
		this.enableTabComplete();
	}

	@Override
	public void run(CommandSender s, String[] args) {
		if (args.length < 2) {
			Util.msgRaw(s, "<red>Usage: /nradmin flag <add/remove> <flag> [player]");
			return;
		}

		String action = args[0].toLowerCase();
		if (!action.equals("add") && !action.equals("remove")) {
			Util.msgRaw(s, "<red>First argument must be 'add' or 'remove'");
			return;
		}

		String flag = args[1];
		Player p;
		if (args.length > 2) {
			p = Bukkit.getPlayer(args[2]);
		} else {
			p = s instanceof Player ? (Player) s : null;
		}

		if (p == null) {
			Util.msgRaw(s, "<red>That player isn't online!");
			return;
		}

		PlayerData pdata = PlayerManager.getPlayerData(p.getUniqueId());
		if (pdata == null) {
			Util.msgRaw(s, "<red>That player has no data!");
			return;
		}

		if (action.equals("add")) {
			pdata.addFlag(flag);
			Util.msgRaw(s, "<green>Added flag '<white>" + flag + "<green>' to " + p.getName());
		} else {
			if (!pdata.hasFlag(flag)) {
				Util.msgRaw(s, "<red>Player " + p.getName() + " doesn't have flag '" + flag + "'");
				return;
			}
			pdata.removeFlag(flag);
			Util.msgRaw(s, "<green>Removed flag '<white>" + flag + "<green>' from " + p.getName());
		}
	}
}
