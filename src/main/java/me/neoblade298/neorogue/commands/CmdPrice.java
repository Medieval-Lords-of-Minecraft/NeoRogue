package me.neoblade298.neorogue.commands;

import java.text.DecimalFormat;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.integration.MaterialPrices;

// Top-level /price command: shows the sell price of the item currently in the player's main hand.
public class CmdPrice extends Subcommand {
	private static final DecimalFormat DF = new DecimalFormat("#,##0.##");

	public CmdPrice(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		ItemStack held = p.getInventory().getItemInMainHand();
		if (held == null || held.getType().isAir()) {
			Util.displayError(p, "You aren't holding anything!");
			return;
		}

		Material mat = held.getType();
		if (!MaterialPrices.hasPrice(mat)) {
			Util.msgRaw(p, "<gray>" + CmdPrices.prettyName(mat) + " <red>can't be sold.");
			return;
		}

		double base = MaterialPrices.getSource().getPrice(mat);
		double current = MaterialPrices.getPrice(mat);
		int amount = held.getAmount();

		Util.msgRaw(p, "<gray>Sell price for <yellow>" + CmdPrices.prettyName(mat) + "<gray>:");
		Util.msgRaw(p, "<gray>- Each: <gold>" + DF.format(current) + CmdPrices.trendSuffix(mat));
		Util.msgRaw(p, "<gray>- Base: <yellow>" + DF.format(base));
		Util.msgRaw(p, "<gray>- Holding <white>" + amount + "<gray>, total: <gold>" + DF.format(current * amount));
	}
}
