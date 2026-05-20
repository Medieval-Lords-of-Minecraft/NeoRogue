package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.session.NodeSelectInstance;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.session.ShopInstance;
import me.neoblade298.neorogue.session.ShrineInstance;
import me.neoblade298.neorogue.session.chance.ChanceInstance;

public class CmdAdminSetInstance extends Subcommand {
	public CmdAdminSetInstance(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		ArrayList<String> tab = new ArrayList<String>(NodeType.values().length);
		for (NodeType type : NodeType.values()) {
			tab.add(type.toString());
		}
		args.add(new Arg("type").setTabOptions(tab), new Arg("chance id", false));
		this.enableTabComplete();
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		Session sess = SessionManager.getSession(p);

		if (sess == null) {
			Util.displayError(p, "You're not currently in a session!");
			return;
		}

		switch (NodeType.valueOf(args[0].toUpperCase())) {
			case NODE_SELECT:
				sess.setInstance(new NodeSelectInstance(sess));
				break;
			case SHOP:
				sess.setInstance(new ShopInstance(sess));
				break;
			case CHANCE:
				sess.setInstance(new ChanceInstance(sess, args.length > 1 ? args[1] : null));
				break;
			case SHRINE:
				sess.setInstance(new ShrineInstance(sess));
				break;
		}
	}

	private static enum NodeType {
		NODE_SELECT, SHOP, CHANCE, SHRINE;
	}
}
