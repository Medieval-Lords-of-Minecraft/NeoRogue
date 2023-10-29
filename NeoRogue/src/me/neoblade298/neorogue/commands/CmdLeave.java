package me.neoblade298.neorogue.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.session.Instance;
import me.neoblade298.neorogue.session.LobbyInstance;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;

public class CmdLeave extends Subcommand {

	public CmdLeave(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		Session sess = SessionManager.getSession(p);
		if (sess == null) {
			Util.displayError(p, "&cYou're not in a session!");
			return;
		}
		
		Instance inst = sess.getInstance();
		if (inst instanceof LobbyInstance) {
			LobbyInstance li = (LobbyInstance) inst;
			li.leavePlayer(p);
			return;
		}
		else {
			sess.leavePlayer(p);
		}
	}
}
