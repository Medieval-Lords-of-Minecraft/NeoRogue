package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.equipment.Equipment;

public class CmdAdminTestEquipment extends Subcommand {
	public CmdAdminTestEquipment(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		this.enableTabComplete();
		args.add(new Arg("id").setTabOptions(new ArrayList<String>(Equipment.getEquipmentIds())),
				new Arg("upgraded (T/F)", false));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player host = (Player) s;
		host.getInventory().addItem(Equipment.get(args[0], args.length > 1 ? args[1].equalsIgnoreCase("t") : false).getItem());
	}
}
