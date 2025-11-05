package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.TrashInventory;
import me.neoblade298.neorogue.session.LobbyInstance;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;

public class CmdAdminTrash extends Subcommand {
	public CmdAdminTrash(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		
		ArrayList<String> tab = new ArrayList<String>();
		for (Equipment eq : Equipment.getAll()) {
			if (eq instanceof Artifact) {
				tab.add(eq.getId());
			}
		}
		args.add(new Arg("player", false), new Arg("artifact", false).setTabOptions(tab));
		this.enableTabComplete();
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player targetPlayer;
		String artifactId = null;
		
		switch (args.length) {
			case 0:
				// 0 args, open the trash for self
				targetPlayer = (Player) s;
				break;
			case 1:
				// 1 arg, either open trash for player or remove artifact from self
				targetPlayer = Bukkit.getPlayer(args[0]);
				if (targetPlayer == null) {
					// Not a player, treat as artifact with sender as target
					targetPlayer = (Player) s;
					artifactId = args[0];
				}
				break;
			case 2:
				// 2 args, remove an artifact from target player
				targetPlayer = Bukkit.getPlayer(args[0]);
				artifactId = args[1];
				break;
			default:
				Util.msg(s, "<red>Too many arguments!");
				return;
		}

		Session sess = SessionManager.getSession(targetPlayer);
		if (sess == null || sess.getInstance() instanceof LobbyInstance) {
			Util.msg(s, "<red>That player isn't in an active session!");
			return;
		}

		PlayerSessionData data = sess.getData(targetPlayer.getUniqueId());
		if (artifactId == null) {
			new TrashInventory(targetPlayer, data);
		} else {
			Artifact art = (Artifact) Equipment.get(artifactId, false);
			data.removeArtifact(art, 1000);
			Util.msg(s, "Removed artifact " + art.getId() + " from " + targetPlayer.getName() + ".");
		}
	}
}
