package me.neoblade298.neorogue.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.ascension.UpgradeTreeMasterInventory;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;

public class CmdUpgrades extends Subcommand {

	public CmdUpgrades(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		Session sess = SessionManager.getSession(p);
		if (sess != null) {
			Util.displayError(p, "You can't do this while in a session!");
			return;
		}
		
		new UpgradeTreeMasterInventory(p, PlayerManager.getPlayerData(p.getUniqueId()));
	}
}
