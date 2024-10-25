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
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;

public class CmdAdminDamage extends Subcommand {
	public CmdAdminDamage(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		ArrayList<String> tab = new ArrayList<String>(DamageType.values().length);
		for (DamageType type : DamageType.values()) {
			tab.add(type.toString());
		}
		args.add(new Arg("damage type").setTabOptions(tab), new Arg("amount"));
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
		DamageType type = DamageType.valueOf(args[0].toUpperCase());
		DamageMeta dm = new DamageMeta(new FightData(), Integer.parseInt(args[1]), type);
		dm.dealDamage(p);
		Util.msg(s, "Dealt " + args[1] + " " + type + " damage");
	}
}
