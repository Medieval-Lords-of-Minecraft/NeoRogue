package me.neoblade298.neorogue.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.NeoRogue;
import net.kyori.adventure.text.Component;

public class CmdAdminDebug extends Subcommand {

	public CmdAdminDebug(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
	}
	
	public void run(CommandSender s, String[] args) {
		Player p = Bukkit.getPlayer("Ascheladd");
		ArmorStand as = (ArmorStand) p.getWorld().spawnEntity(p.getLocation().add(0, 0, 3), EntityType.ARMOR_STAND);
		as.setInvisible(true);
		as.setInvulnerable(true);
		as.setSmall(true);
		as.setGravity(true);
		as.customName(Component.text("Test123"));
		as.setCustomNameVisible(true);
		as.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 60, 0));
		new BukkitRunnable() {
			public void run() {
				as.remove();
			}
		}.runTaskLater(NeoRogue.inst(), 40L);
	}
}
