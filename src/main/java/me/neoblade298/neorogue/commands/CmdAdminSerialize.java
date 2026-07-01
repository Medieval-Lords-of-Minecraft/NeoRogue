package me.neoblade298.neorogue.commands;

import java.util.ArrayList;
import java.util.List;

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
		this.overrideTabHandler();
		args.add(new Arg("player", false));
		args.add(new Arg("id", false));
	}

	@Override
	public List<String> getTabOptions(CommandSender s, String[] args) {
		if (!(s instanceof Player) && args.length <= 1) {
			ArrayList<String> players = new ArrayList<>();
			for (Player p : Bukkit.getOnlinePlayers()) players.add(p.getName());
			return players;
		}
		return EquipmentPresets.getNames();
	}

	public void run(CommandSender s, String[] args) {
		Player p;
		String presetId = null;
		
		if (args.length >= 1 && Bukkit.getPlayer(args[0]) != null) {
			p = Bukkit.getPlayer(args[0]);
			if (args.length >= 2) presetId = args[1];
		} else if (args.length >= 1) {
			// First arg is not an online player, treat it as an id
			if (!(s instanceof Player)) {
				s.sendMessage("Must specify a player when running from console!");
				return;
			}
			p = (Player) s;
			presetId = args[0];
		} else {
			if (!(s instanceof Player)) {
				s.sendMessage("Must specify a player when running from console!");
				return;
			}
			p = (Player) s;
		}
		
		Session sess = SessionManager.getSession(p);

		if (sess == null) {
			Util.displayError(p, "Player is not currently in a session!");
			return;
		}

		PlayerSessionData data = sess.getParty().get(p.getUniqueId());
		String serialized = data.serialize();
		
		if (presetId != null) {
			if (EquipmentPresets.get(presetId) != null) {
				Util.msgRaw(s, "<red>Preset '" + presetId + "' already exists. Use a different name.");
				return;
			}
			EquipmentPresets.save(presetId, serialized);
			Util.msgRaw(s, "Saved " + p.getName() + "'s equipment as preset: " + presetId);
			return;
		}
		
		Component cmp;
		if (s instanceof Player) {
			cmp = Component.text("Click here to copy " + p.getName() + "'s equipment to clipboard")
				.clickEvent(ClickEvent.copyToClipboard(serialized))
				.hoverEvent(HoverEvent.showText(Component.text("Click to copy").color(NamedTextColor.YELLOW)));
		}
		else {
			cmp = Component.text(p.getName() + "'s Data:").appendNewline().append(Component.text(serialized));
		}
		Util.msgRaw(s, cmp);
	}
}
