package me.neoblade298.neorogue.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;

public class CmdAdminDeserialize extends Subcommand {

	public CmdAdminDeserialize(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("player", false));
		args.add(new Arg("data"));
		args.setMax(-1);
	}

	public void run(CommandSender s, String[] args) {
		Player p = args.length >= 1 ? Bukkit.getPlayer(args[0]) : (Player) s;
		Session sess = SessionManager.getSession(p);

		if (sess == null) {
			Util.displayError(p, "Player is not currently in a session!");
			return;
		}

		PlayerSessionData data = sess.getParty().get(p.getUniqueId());
		
		try {
			data.deserialize(args.length > 1 ? SharedUtil.connectArgs(args, 1) : SharedUtil.connectArgs(args, 0));
			data.setupInventory();
			data.updateBoardLines();
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}
		Util.msg(s, "Loaded equipment data for " + p.getName());
	}
}
