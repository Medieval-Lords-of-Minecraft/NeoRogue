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
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;

public class CmdAdminDeserialize extends Subcommand {

	public CmdAdminDeserialize(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		this.overrideTabHandler();
		args.add(new Arg("player", false));
		args.add(new Arg("preset-name|raw-data"));
		args.setMax(-1);
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

	@Override
	public void run(CommandSender s, String[] args) {
		Player p;
		int dataStart;
		if (args.length >= 1 && Bukkit.getPlayer(args[0]) != null) {
			p = Bukkit.getPlayer(args[0]);
			dataStart = 1;
		} else if (s instanceof Player) {
			p = (Player) s;
			dataStart = 0;
		} else {
			Util.msgRaw(s, "<red>Usage: deserialize [player] <preset-name|raw-data>");
			return;
		}

		if (dataStart >= args.length) {
			Util.msgRaw(s, "<red>You must provide a preset name or raw data.");
			return;
		}

		Session sess = SessionManager.getSession(p);
		if (sess == null) {
			Util.msgRaw(s, "<red>Player is not currently in a session!");
			return;
		}

		String rawInput = SharedUtil.connectArgs(args, dataStart);
		String resolvedData = EquipmentPresets.get(rawInput);
		if (resolvedData == null) resolvedData = rawInput;

		PlayerSessionData pdata = sess.getParty().get(p.getUniqueId());
		try {
			pdata.deserialize(resolvedData);
			pdata.setupInventory();
			pdata.updateBoardLines();
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}
		Util.msgRaw(s, "Loaded equipment data for " + p.getName());
	}
}
