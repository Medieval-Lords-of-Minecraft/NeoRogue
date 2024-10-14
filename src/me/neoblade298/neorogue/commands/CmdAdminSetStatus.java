package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.session.Instance;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;

public class CmdAdminSetStatus extends Subcommand {
	public CmdAdminSetStatus(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		ArrayList<String> tab = new ArrayList<String>(StatusType.values().length);
		for (StatusType type : StatusType.values()) {
			tab.add(type.toString());
		}
		args.add(new Arg("status").setTabOptions(tab), new Arg("stacks"), new Arg("duration", false));
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

		Instance inst = sess.getInstance();
		if (!(inst instanceof FightInstance)) {
			Util.displayError(p, "You're not currently in a fight!");
			return;
		}
		PlayerFightData data = FightInstance.getUserData(p.getUniqueId());
		StatusType type = StatusType.valueOf(args[0].toUpperCase());
		data.applyStatus(type, data, Integer.parseInt(args[1]), args.length > 2 ? Integer.parseInt(args[2]) : -1);
		Util.msg(s, "Applied status " + type + " with " + args[1] + " stacks.");
	}
}
