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
	private static final ArrayList<String> notorietyTab = new ArrayList<String>();
	static {
		for (int i = 0; i <= 10; i++) notorietyTab.add(String.valueOf(i));
	}

	public CmdAdminTestHF(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.setMax(-1);
		args.add(new Arg("Host"), new Arg("notoriety (0-10)", false).setTabOptions(notorietyTab),
				new Arg("Players", false));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player host = Bukkit.getPlayer(args[0]);
		ArrayList<Player> others;

		// An optional notoriety (0-10) may follow the host; anything else is treated as a player name
		// so existing usages that list players immediately after the host keep working.
		int notoriety = 0;
		int playerStart = 1;
		if (args.length > 1) {
			try {
				notoriety = Math.max(0, Math.min(Integer.parseInt(args[1]), 10));
				playerStart = 2;
			}
			catch (NumberFormatException ignored) {}
		}

		if (args.length > playerStart) {
			others = new ArrayList<Player>();
			for (int i = playerStart; i < args.length; i++) {
				others.add(Bukkit.getPlayer(args[i]));
			}
		}
		else {
			others = new ArrayList<Player>(Bukkit.getOnlinePlayers());
		}

		NeoRogue.debugInitialize(host, others, EquipmentClass.WARRIOR, RegionType.HARVEST_FIELDS, notoriety);
	}
}
