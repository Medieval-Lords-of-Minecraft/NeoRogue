package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;

public class CmdAdminTestEquipment extends Subcommand {
	public CmdAdminTestEquipment(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		this.enableTabComplete();
		ArrayList<String> tf = new ArrayList<String>(2);
		tf.add("T");
		tf.add("F");
		args.add(new Arg("id").setTabOptions(new ArrayList<String>(Equipment.getEquipmentIds())),
				new Arg("upgraded (T/F)", false).setTabOptions(tf));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		Session sess = SessionManager.getSession(p);
		Equipment eq = Equipment.get(args[0], args.length > 1 ? args[1].equalsIgnoreCase("t") : false);
		if (sess == null) {
			p.getInventory().addItem(eq.getItem());
		}
		else {
			sess.getParty().get(p.getUniqueId()).giveEquipment(eq);
		}
	}
}
