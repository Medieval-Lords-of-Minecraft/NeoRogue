package me.neoblade298.neorogue.commands;

import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;

public class CmdAdminTest extends Subcommand {
	public CmdAdminTest(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("Arg"), new Arg("Arg2, false"));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		if (args[0].equalsIgnoreCase("hf")) {
			NeoRogue.debugInitialize();
		}
		else if (args[0].equalsIgnoreCase("dmg")) {
			new BukkitRunnable() {
				public void run() {
					p.damage(5, p);
				}
			}.runTaskLater(NeoRogue.inst(), 20);
		}
		else {
			ArrayList<Equipment> drop = Equipment.getDrop(Integer.parseInt(args[0]), Integer.parseInt(args[1]), EquipmentClass.WARRIOR, EquipmentClass.CLASSLESS);
			Collections.sort(drop);
			for (Equipment eq : drop) {
				System.out.println(eq);
			}
			System.out.println("-----");
		}
	}
}
