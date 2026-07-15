package me.neoblade298.neorogue.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.lumine.mythic.api.exceptions.InvalidMobTypeException;
import io.lumine.mythic.bukkit.MythicBukkit;
import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;

public class CmdAdminTest extends Subcommand {

	public CmdAdminTest(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
	}

	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		for (int i = 0; i < 10; i++) {
			try {
				MythicBukkit.inst().getAPIHelper().spawnMythicMob("SewerMonster", p.getLocation());
			} catch (InvalidMobTypeException e) {
				e.printStackTrace();
			}
		}
	}
}
