package me.neoblade298.neorogue.commands;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.Sounds;

public class CmdAdminDebug extends Subcommand {
	HashMap<String, HashMap<String, Integer>> results = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, HashMap<String, Integer>> failedResults = new HashMap<String, HashMap<String, Integer>>();
	HashSet<String> resultKeys = new HashSet<String>();

	public CmdAdminDebug(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		Sounds.equip.play(p, p);
		ThrownPotion thrown = (ThrownPotion) p.getWorld().spawnEntity(p.getLocation().add(0, 1, 0), EntityType.SPLASH_POTION);
		thrown.setVelocity(p.getEyeLocation().getDirection());
		ItemStack item = new ItemStack(Material.POTION);
		thrown.setItem(item);
	}
}
