package me.neoblade298.neorogue.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;

public class CmdInvite extends Subcommand {

	public CmdInvite(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		Util.msgRaw(p, NeoCore.miniMessage().deserialize(
				"<gray>Invites have been replaced! Players can request to join your lobby by running "
				+ "<yellow>/nr join " + p.getName() + "</yellow>, which you can then accept or decline."));
	}
}
