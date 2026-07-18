package me.neoblade298.neorogue.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.inventory.CargoInventory;
import me.neoblade298.neorogue.session.SessionManager;

public class CmdCargo extends Subcommand {

	public CmdCargo(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		if (SessionManager.getSession(p) != null) {
			Util.displayError(p, "You can't manage cargo during a run!");
			return;
		}
		PlayerData pd = PlayerManager.getPlayerData(p.getUniqueId());
		if (pd == null) {
			Util.displayError(p, "Your player data isn't loaded yet!");
			return;
		}
		if (!pd.hasFlag(PlayerData.FLAG_CARGO_ACCESS)) {
			Util.displayError(p, "You haven't unlocked cargo access yet! Buy it from the Caravan Upgrades menu.");
			return;
		}
		new CargoInventory(p, pd);
	}
}
