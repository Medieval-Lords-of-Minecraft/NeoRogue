package me.neoblade298.neorogue.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.session.instances.Instance;
import me.neoblade298.neorogue.session.instances.LobbyInstance;

public class CmdAccept extends Subcommand {

	public CmdAccept(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("player"));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		Session sess = SessionManager.getSession(p);
		if (sess == null) {
			Util.displayError(p, "You're not in a session!");
			return;
		}

		Instance inst = sess.getInstance();
		if (!(inst instanceof LobbyInstance)) {
			Util.displayError(p, "You can't do that right now!");
			return;
		}

		((LobbyInstance) inst).acceptRequest(p, args[0]);
	}
}
