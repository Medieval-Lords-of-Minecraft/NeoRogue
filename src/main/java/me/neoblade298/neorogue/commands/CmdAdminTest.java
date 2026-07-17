package me.neoblade298.neorogue.commands;

import org.bukkit.block.Block;
import org.bukkit.block.Lectern;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;

public class CmdAdminTest extends Subcommand {

	public CmdAdminTest(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
	}

	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		Block target = p.getTargetBlockExact(10);
		if (target == null || !(target.getState() instanceof Lectern)) {
			Util.msgRaw(p, "<red>You aren't looking at a lectern!");
			return;
		}

		Lectern lectern = (Lectern) target.getState(false);
		ItemStack book = lectern.getInventory().getItem(0);
		if (book == null || book.getType().isAir()) {
			Util.msgRaw(p, "<yellow>That lectern didn't have a book.");
			return;
		}

		lectern.getInventory().clear();
		Util.msgRaw(p, "<green>Cleared the book from the lectern.");
	}
}
