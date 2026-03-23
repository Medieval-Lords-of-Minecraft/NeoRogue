package me.neoblade298.neorogue.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.session.Session;

public class CmdAdminDebugMode extends Subcommand {

	public CmdAdminDebugMode(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.setMax(-1);
		args.setMin(-1);
	}

	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
        Session sess = SessionManager.getSession(p);
        
        if(sess = null) {
            Util.displayError(p, "You're not currently in a session"!);
        }
        
        if(sess.isDebug()) {
            sess.setDebug(false);
            Util.msg(s, "Debug Mode Disabled");
        } else {
            sess.setDebug(true);
            Util.msg(s, "Debug Mode Enabled");
        }
	}
}
