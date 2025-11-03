package me.neoblade298.neorogue.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;

public class CmdAdminTest extends Subcommand {
	public CmdAdminTest(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("Arg"));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		Session sess = SessionManager.getSession(p);
		if (sess != null) {
			Util.msg(s, "<red>You're already in an active session!");
			return;
		}

		if (args[0].equalsIgnoreCase("hf")) {
			NeoRogue.debugInitialize();
		}
		else {
			for (Equipment eq : Equipment.getDrop(Integer.parseInt(args[0]), 4, EquipmentClass.WARRIOR, EquipmentClass.CLASSLESS)) {
				System.out.println(eq);
			}
		}
	}
}
