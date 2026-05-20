package me.neoblade298.neorogue.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class CmdAdminSerialize extends Subcommand {

	public CmdAdminSerialize(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("player", false));
	}

	public void run(CommandSender s, String[] args) {
		Player p = args.length >= 1 ? Bukkit.getPlayer(args[0]) : (Player) s;
		Session sess = SessionManager.getSession(p);

		if (sess == null) {
			Util.displayError(p, "Player is not currently in a session!");
			return;
		}

		PlayerSessionData data = sess.getParty().get(p.getUniqueId());
		Component cmp;
		if (s instanceof Player) {
			cmp = Component.text("Click here to copy " + p.getName() + "'s equipment to clipboard")
				.clickEvent(ClickEvent.copyToClipboard(data.serialize()))
				.hoverEvent(HoverEvent.showText(Component.text("Click to copy").color(NamedTextColor.YELLOW)));
		}
		else {
			cmp = Component.text(p.getName() + "'s Data:").appendNewline().append(Component.text(data.serialize()));
		}
		Util.msg(s, cmp);
	}
}
