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

public class CmdInvite extends Subcommand {

	public CmdInvite(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("username/all"));
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
			Util.displayError(p, "You can't invite anyone at this time!");
			return;
		}
		
		LobbyInstance li = (LobbyInstance) inst;
		if (args[0].equalsIgnoreCase("all")) {
			for (Player on : Bukkit.getOnlinePlayers()) {
				if (SessionManager.getSession(on) != null) continue;
				li.invitePlayer(p, on.getName());
			}
		}
		else {
			li.invitePlayer(p, args[0]);
		}
	}
}
