package me.neoblade298.neorogue.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.session.LobbyInstance;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;

public class CmdJoin extends Subcommand {

	public CmdJoin(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("party name"));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		if (SessionManager.getSession(p) != null) {
			Util.msg(s, "&cYou're already in a session!");
			return;
		}
		
		for (Session sess : SessionManager.getSessions()) {
			if (sess.getInstance() instanceof LobbyInstance) {
				LobbyInstance li = (LobbyInstance) sess.getInstance();
				if (li.getName().equals(args[0])) {
					li.addPlayer(p);
					return;
				}
			}
		}
		
		Util.msg(s, "&cThat lobby doesn't exist!");
	}
}