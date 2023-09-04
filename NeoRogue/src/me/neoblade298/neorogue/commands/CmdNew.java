package me.neoblade298.neorogue.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.session.SessionManager;

public class CmdNew extends Subcommand {

	public CmdNew(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("save slot", false), new Arg("party name", false));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		if (SessionManager.getSession(p) != null) {
			Util.msg(s, "&cYou're already in a session!");
			return;
		}
		
		if (args.length == 0) {
			PlayerManager.getPlayerData(p.getUniqueId()).displayNewButtons(s);
		}
		else if (args.length == 2) {
			SessionManager.createSession(p, args[1], Integer.parseInt(args[0])); 
		}
		else {
			Util.msg(s, "&cYour command is missing an argument!");
		}
	}
}
