package me.neoblade298.neorogue.commands;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;

public class CmdAdminTest extends Subcommand {

	public CmdAdminTest(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		this.enableTabComplete();
	}

	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		Block b = p.getTargetBlockExact(5);
		if (b == null || b.getType() != Material.LECTERN) {
			Util.displayError(p, "You must be looking at a lectern!");
			return;
		}

		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta meta = (BookMeta) book.getItemMeta();
		meta.setAuthor("MLMC");
		meta.setTitle("Fight Info");
		book.setItemMeta(meta);

		// Write to the live tile entity (getState(false)) rather than a snapshot so the change persists
		if (!(b.getState(false) instanceof org.bukkit.block.Lectern lec)) {
			Util.displayError(p, "Failed to access the lectern!");
			return;
		}
		lec.getInventory().clear();
		lec.getInventory().addItem(book);
		Util.msg(p, "Placed a book into the lectern.");
	}
}
