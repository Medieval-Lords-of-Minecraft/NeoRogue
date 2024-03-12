package me.neoblade298.neorogue.commands;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.NeoRogue;

public class CmdAdminDebug extends Subcommand {
	HashMap<String, HashMap<String, Integer>> results = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, HashMap<String, Integer>> failedResults = new HashMap<String, HashMap<String, Integer>>();
	HashSet<String> resultKeys = new HashSet<String>();

	public CmdAdminDebug(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("damage", false));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = Bukkit.getPlayer("Ascheladd");
		new BukkitRunnable() {
			int count = 0;
			public void run() {
				p.damage(1);
				count++;
				if (count > 5) {
					cancel();
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 10l, 2l);
	}
}
