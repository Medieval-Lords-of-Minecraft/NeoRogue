package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CmdAdminDrop extends Subcommand {
	public CmdAdminDrop(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		ArrayList<String> ecTab = new ArrayList<>();
		for (EquipmentClass ec : EquipmentClass.values()) {
			ecTab.add(ec.name());
		}
		args.add(new Arg("value"), new Arg("amount"), new Arg("classes...").setTabOptions(ecTab));
		this.enableTabComplete();
	}

	@Override
	public void run(CommandSender s, String[] args) {
		int value;
		try {
			value = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			Util.msg(s, "<red>Invalid value: " + args[0]);
			return;
		}

		int amount = 1;
		if (args.length > 1) {
			try {
				amount = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				Util.msg(s, "<red>Invalid amount: " + args[1]);
				return;
			}
		}

		EquipmentClass[] ecs;
		if (args.length > 2) {
			ecs = new EquipmentClass[args.length - 2];
			for (int i = 2; i < args.length; i++) {
				try {
					ecs[i - 2] = EquipmentClass.valueOf(args[i].toUpperCase());
				} catch (IllegalArgumentException e) {
					Util.msg(s, "<red>Invalid equipment class: " + args[i]);
					return;
				}
			}
		} else {
			ecs = new EquipmentClass[] { EquipmentClass.CLASSLESS };
		}

		ArrayList<Equipment> drops = Equipment.getDrop(value, amount, ecs);
		Util.msg(s, "<yellow>Rolled " + amount + " drop(s) at value " + value + ":");
		for (Equipment eq : drops) {
			Util.msgRaw(s, Component.text("- ", NamedTextColor.GRAY).append(eq.getHoverable()));
		}
	}
}
