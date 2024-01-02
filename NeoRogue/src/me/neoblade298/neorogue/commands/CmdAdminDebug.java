package me.neoblade298.neorogue.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;

public class CmdAdminDebug extends Subcommand {

	public CmdAdminDebug(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
	}

	@Override
	public void run(CommandSender s, String[] args) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (!p.getName().equals("Ascheladd")) continue;
			p.setInvisible(false);
			p.setInvulnerable(false);
			System.out.println("1: " + p.getAbsorptionAmount());
			p.setAbsorptionAmount(20.0);
			System.out.println("2: " + p.getAbsorptionAmount());
			p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 6));
			System.out.println("3: " + p.getAbsorptionAmount());
			p.setAbsorptionAmount(4.0);
			System.out.println("4: " + p.getAbsorptionAmount());
		}
		Util.msg(s, "Ran debug command");
	}
}
