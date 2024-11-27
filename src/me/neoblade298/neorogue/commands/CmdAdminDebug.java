package me.neoblade298.neorogue.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;

public class CmdAdminDebug extends Subcommand {

	public CmdAdminDebug(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.setMax(-1);
		args.setMin(-1);
	}

	public void run(CommandSender s, String[] args) {
		Player p = args.length >= 1 ? Bukkit.getPlayer(args[0]) : (Player) s;
		Session sess = SessionManager.getSession(p);

		if (sess == null) {
			Util.displayError(p, "Player is not currently in a session!");
			return;
		}

		PlayerSessionData data = sess.getParty().get(p.getUniqueId());
		for (EquipSlot es : EquipSlot.values()) {
			String line = es + ": ";
			Equipment[] eqs = data.getEquipment(es);
			for (int i = 0; i < eqs.length; i++) {
				Equipment eq = eqs[i];
				line += i + (eq == null ? "-" : eq.toString()) + " ";
			}
			Util.msg(p, line);
		}
	}
}
