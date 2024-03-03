package me.neoblade298.neorogue.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;

public class CmdSpectate extends Subcommand {

	public CmdSpectate(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("player"));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		Player target = Bukkit.getPlayer(args[0]);
		if (SessionManager.getSession(p) != null) {
			Util.displayError(p, "You're already in a session!");
			return;
		}
		if (target == null) {
			Util.displayError(p, "That player isn't online!");
			return;
		}

		Session sess = SessionManager.getSession(target);
		if (sess == null) {
			Util.displayError(p, "That player isn't in a session!");
			return;
		}
		if (sess.isBusy()) {
			Util.displayError(p, "You can't do that while the session is loading!");
			return;
		}
		sess.addSpectator(p);
	}
}
