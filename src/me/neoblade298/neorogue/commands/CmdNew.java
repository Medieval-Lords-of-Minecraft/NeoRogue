package me.neoblade298.neorogue.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.session.SessionManager;

public class CmdNew extends Subcommand {

	public CmdNew(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("save slot", false), new Arg("party name", false));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		if (SessionManager.getSession(p) != null) {
			Util.displayError(p, "You're already in a session!");
			return;
		}
		
		PlayerData pd = PlayerManager.getPlayerData(p.getUniqueId());
		if (args.length == 0) {
			pd.displayNewButtons(s);
			return;
		}
		
		int slot = Integer.parseInt(args[0]);
		if (slot <= 0 || slot > pd.getSlots()) {
			Util.displayError(p, "Invalid save slot! You only have " + pd.getSlots() + " slot(s)!");
			return;
		}
		
		if (args.length == 2) {
			SessionManager.createSession(p, args[1], slot, true); 
		}
		else if (args.length == 1) {
			SessionManager.createSession(p, p.getName() + "Party", slot, true); 
		}
	}
}
