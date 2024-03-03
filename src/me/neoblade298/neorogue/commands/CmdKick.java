package me.neoblade298.neorogue.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.session.Instance;
import me.neoblade298.neorogue.session.LobbyInstance;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;

public class CmdKick extends Subcommand {

	public CmdKick(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("username"));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		Session sess = SessionManager.getSession(p);
		if (sess == null) {
			Util.displayError(p, "You're not in a session!");
			return;
		}
		if (sess.isBusy()) {
			Util.displayError(p, "You can't do that while the session is loading!");
			return;
		}
		
		Instance inst = sess.getInstance();
		if (inst instanceof LobbyInstance) {
			LobbyInstance li = (LobbyInstance) inst;
			li.kickPlayer(p, args[0]);
		}
		else {
			sess.kickPlayer(p, Bukkit.getOfflinePlayer(args[0]));
		}
		
	}
}
