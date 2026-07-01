package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;

public class CmdAdminSetPoints extends Subcommand {
	public CmdAdminSetPoints(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		ArrayList<String> classTab = new ArrayList<String>();
		classTab.add("GLOBAL");
		classTab.add("WARRIOR");
		classTab.add("THIEF");
		classTab.add("MAGE");
		classTab.add("ARCHER");
		args.add(new Arg("amount"), new Arg("class", false).setTabOptions(classTab), new Arg("player", false));
		this.enableTabComplete();
	}

	@Override
	public void run(CommandSender s, String[] args) {
		EquipmentClass ec = null;
		Player p;
		if (args.length > 2) {
			p = Bukkit.getPlayer(args[2]);
		} else if (args.length > 1 && Bukkit.getPlayer(args[1]) != null && !isClassArg(args[1])) {
			p = Bukkit.getPlayer(args[1]);
		} else {
			p = s instanceof Player ? (Player) s : null;
		}
		if (args.length > 1 && isClassArg(args[1])) {
			ec = parseClass(args[1]);
		}
		if (p == null) {
			Util.msgRaw(s, "<red>That player isn't online!");
			return;
		}
		PlayerData pdata = PlayerManager.getPlayerData(p.getUniqueId());
		if (pdata == null) {
			Util.msgRaw(s, "<red>That player has no data!");
			return;
		}
		int amount = Integer.parseInt(args[0]);
		pdata.setPoints(ec, amount);
		String category = ec == null ? "Global" : ec.getDisplay();
		Util.msgRaw(s, "<green>Set " + category + " points to " + amount + " for " + p.getName());
	}

	private boolean isClassArg(String arg) {
		return arg.equalsIgnoreCase("GLOBAL") || arg.equalsIgnoreCase("WARRIOR")
				|| arg.equalsIgnoreCase("THIEF") || arg.equalsIgnoreCase("MAGE")
				|| arg.equalsIgnoreCase("ARCHER");
	}

	private EquipmentClass parseClass(String arg) {
		if (arg.equalsIgnoreCase("GLOBAL")) return null;
		return EquipmentClass.valueOf(arg.toUpperCase());
	}
}
