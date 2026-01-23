package me.neoblade298.neorogue.commands;

import org.bukkit.command.CommandSender;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.session.ShopContents;

public class CmdAdminDebug extends Subcommand {

	public CmdAdminDebug(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.setMax(-1);
		args.setMin(-1);
	}

	public void run(CommandSender s, String[] args) {
		ShopContents.debugGenerateEquips();
	}
}
