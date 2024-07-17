package me.neoblade298.neorogue.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import io.lumine.mythic.api.exceptions.InvalidMobTypeException;
import io.lumine.mythic.bukkit.MythicBukkit;
import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.NeoRogue;

public class CmdAdminDebug extends Subcommand {

	public CmdAdminDebug(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		Damageable e;
		try {
			e = (Damageable) MythicBukkit.inst().getAPIHelper().spawnMythicMob("Trickster", p.getLocation());
			e.damage(5);
			DamageSource src = DamageSource.builder(org.bukkit.damage.DamageType.GENERIC).withDirectEntity(p).build();
			
			new BukkitRunnable() {
				public void run() {
					e.damage(5, src);
					Util.msg(p, "Part 2");
				}
			}.runTaskLater(NeoRogue.inst(), 20L);
			
			new BukkitRunnable() {
				public void run() {
					e.damage(5, p);
					Util.msg(p, "Part 3");
				}
			}.runTaskLater(NeoRogue.inst(), 40L);
		} catch (InvalidMobTypeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
}
