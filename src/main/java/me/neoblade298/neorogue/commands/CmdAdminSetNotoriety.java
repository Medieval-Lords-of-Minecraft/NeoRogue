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

public class CmdAdminSetNotoriety extends Subcommand {
	public CmdAdminSetNotoriety(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		ArrayList<String> classTab = new ArrayList<String>();
		classTab.add("WARRIOR");
		classTab.add("THIEF");
		classTab.add("MAGE");
		classTab.add("ARCHER");
		args.add(new Arg("level"), new Arg("class").setTabOptions(classTab), new Arg("player", false));
		this.enableTabComplete();
	}

	@Override
	public void run(CommandSender s, String[] args) {
		if (args.length < 2) {
			Util.msgRaw(s, "<red>Usage: setnotoriety <level> <class> [player]");
			return;
		}
		Player p;
		if (args.length > 2) {
			p = Bukkit.getPlayer(args[2]);
		} else {
			p = s instanceof Player ? (Player) s : null;
		}
		if (p == null) {
			Util.msgRaw(s, "<red>That player isn't online!");
			return;
		}
		EquipmentClass ec;
		try {
			ec = EquipmentClass.valueOf(args[1].toUpperCase());
		} catch (IllegalArgumentException e) {
			Util.msgRaw(s, "<red>Unknown class: " + args[1] + ". Use WARRIOR, THIEF, MAGE, or ARCHER.");
			return;
		}
		PlayerData pdata = PlayerManager.getPlayerData(p.getUniqueId());
		if (pdata == null) {
			Util.msgRaw(s, "<red>That player has no data!");
			return;
		}
		int level;
		try {
			level = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			Util.msgRaw(s, "<red>Invalid number: " + args[0]);
			return;
		}
		pdata.setNotorietyMax(ec, level);
		Util.msgRaw(s, "<green>Set " + ec.getDisplay() + " max notoriety to " + level + " for " + p.getName()
				+ " (current max notoriety: " + pdata.getMaxNotoriety(ec) + ")");
	}
}
