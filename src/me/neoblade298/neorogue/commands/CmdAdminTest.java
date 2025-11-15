package me.neoblade298.neorogue.commands;

import org.bukkit.command.CommandSender;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.NeoRogue;

public class CmdAdminTest extends Subcommand {
	public CmdAdminTest(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("Arg"), new Arg("Arg2", false));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		if (args[0].equalsIgnoreCase("hf")) {
			NeoRogue.debugInitialize();
		}
	}
}
