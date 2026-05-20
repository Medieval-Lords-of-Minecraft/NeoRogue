package me.neoblade298.neorogue.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.session.LobbyInstance;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;

public class CmdAdminCoins extends Subcommand {
	public CmdAdminCoins(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("amount"));
		args.add(new Arg("player", false));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = args.length > 1 ? Bukkit.getPlayer(args[1]) : (Player) s;
		if (p == null) {
			Util.msg(s, "<red>That player isn't online!");
			return;
		}
		Session sess = SessionManager.getSession(p);
		if (sess == null || sess.getInstance() instanceof LobbyInstance) {
			Util.msg(s, "<red>That player isn't in an active session!");
			return;
		}
		sess.getParty().get(p.getUniqueId()).addCoins(Integer.parseInt(args[0]));
	}
}
