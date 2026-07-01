package me.neoblade298.neorogue.commands;

import org.bukkit.command.CommandSender;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CmdHelp extends Subcommand {

	public CmdHelp(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Util.msgRaw(s, Component.text("NeoRogue Commands", NamedTextColor.DARK_RED));
		sendLine(s, "/nr", "Open the main menu");
		sendLine(s, "/nr help", "View this command list");
		sendLine(s, "/nr new [slot]", "Create a new game");
		sendLine(s, "/nr load [slot]", "Load an existing game");
		sendLine(s, "/nr invite <player>", "Invite a player to your party");
		sendLine(s, "/nr join", "Join an active session");
		sendLine(s, "/nr leave", "Leave your session");
		sendLine(s, "/nr kick <player>", "Kick a player from your party");
		sendLine(s, "/nr spectate", "Spectate a player's session");
		sendLine(s, "/nr info", "View session info");
		sendLine(s, "/nr list", "View a filtered list of equipment");
		sendLine(s, "/nr glossary", "View glossary");
	}

	private void sendLine(CommandSender s, String cmd, String desc) {
		Util.msgRaw(s, Component.text(cmd, NamedTextColor.GOLD)
				.append(Component.text(" - " + desc, NamedTextColor.GRAY)));
	}
}
