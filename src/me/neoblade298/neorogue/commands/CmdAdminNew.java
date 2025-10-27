package me.neoblade298.neorogue.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;

public class CmdAdminNew extends Subcommand {

	public CmdAdminNew(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		if (SessionManager.getSession(p) != null) {
			Util.displayError(p, "You're already in a session!");
			return;
		}
		
		Session sess = SessionManager.createSession(p, "test", 1);
		sess.generateArea(AreaType.HARVEST_FIELDS);
		sess.setNodesVisited(15);
		sess.setAreasCompleted(1);
		for (Player pl : Bukkit.getOnlinePlayers()) {
			sess.addPlayer(pl.getUniqueId(), EquipmentClass.WARRIOR);
			SessionManager.addToSession(pl.getUniqueId(), sess);
		}
		sess.setNode(sess.getArea().getNodes()[0][2]);
	}
}
