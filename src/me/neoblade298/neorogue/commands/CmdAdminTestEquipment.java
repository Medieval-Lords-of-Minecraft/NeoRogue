package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.LobbyInstance;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;

public class CmdAdminTestEquipment extends Subcommand {
	public CmdAdminTestEquipment(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		this.enableTabComplete();
		ArrayList<String> tab = new ArrayList<String>(Equipment.getEquipmentIds().size() * 2);
		for (Equipment eq : Equipment.getAll()) {
			tab.add(eq.getId());
			if (eq.getUpgraded() != null) {
				tab.add(eq.getId() + "+");
			}
		}
		args.add(new Arg("id").setTabOptions(new ArrayList<String>(tab)));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		Session sess = SessionManager.getSession(p);
		String id = args[0];
		boolean upgrade = false;
		if (id.endsWith("+")) {
			id = id.substring(0, id.length() - 1);
			upgrade = true;
		}
		Equipment eq = Equipment.get(id, upgrade);
		if (eq == null) {
			Util.displayError(p, "That equipment doesn't exist!");
		}
		if (sess == null || sess.getInstance() instanceof LobbyInstance) {
			p.getInventory().addItem(eq.getItem());
		}
		else {
			sess.getParty().get(p.getUniqueId()).giveEquipment(eq);
		}
	}
}
