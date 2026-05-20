package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.region.RegionType;

public class CmdAdminTestHF extends Subcommand {
	public CmdAdminTestHF(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.setMax(-1);
		args.add(new Arg("Host"), new Arg("Players", false));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player host = Bukkit.getPlayer(args[0]);
		ArrayList<Player> others;

		if (args.length > 1) {
			others = new ArrayList<Player>();
			for (int i = 1; i < args.length; i++) {
				others.add(Bukkit.getPlayer(args[i]));
			}
		}
		else {
			others = new ArrayList<Player>(Bukkit.getOnlinePlayers());
		}

		NeoRogue.debugInitialize(host, others, EquipmentClass.THIEF, RegionType.HARVEST_FIELDS);
	}
}
