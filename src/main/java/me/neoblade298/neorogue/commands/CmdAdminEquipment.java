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
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.session.instances.LobbyInstance;

public class CmdAdminEquipment extends Subcommand {
	public CmdAdminEquipment(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		this.enableTabComplete();
		ArrayList<String> tab = new ArrayList<String>(Equipment.getEquipmentIds().size() * 2);
		for (Equipment eq : Equipment.getAll()) {
			tab.add(eq.getId());
			if (eq.getUpgraded() != null) {
				tab.add(eq.getId() + "+");
			}
		}
		args.add(new Arg("id").setTabOptions(new ArrayList<String>(tab)));
		args.add(new Arg("player", false));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = args.length > 1 ? Bukkit.getPlayer(args[1]) : (Player) s;
		if (p == null) {
			Util.msg(s, "<red>That player isn't online!");
			return;
		}
		Session sess = SessionManager.getSession(p);
		String id = args[0];

		// Holder syntax: "HolderId:HeldId" grants a holder artifact (e.g. EchoStone)
		// holding the specified equipment, e.g. "EchoStone:FlowState" or "EchoStone:FlowState+".
		String heldSpec = null;
		int colon = id.indexOf(':');
		if (colon >= 0) {
			heldSpec = id.substring(colon + 1);
			id = id.substring(0, colon);
		}

		boolean upgrade = false;
		if (id.endsWith("+")) {
			id = id.substring(0, id.length() - 1);
			upgrade = true;
		}
		Equipment eq = Equipment.get(id, upgrade);
		if (eq == null) {
			Util.displayError(p, "That equipment doesn't exist!");
			return;
		}

		if (heldSpec != null) {
			if (!(eq instanceof Artifact)) {
				Util.displayError(p, "Only holder artifacts (e.g. EchoStone) can hold equipment!");
				return;
			}
			boolean heldUpgrade = false;
			String heldId = heldSpec;
			if (heldId.endsWith("+")) {
				heldId = heldId.substring(0, heldId.length() - 1);
				heldUpgrade = true;
			}
			Equipment held = Equipment.get(heldId, heldUpgrade);
			if (held == null) {
				Util.displayError(p, "The held equipment doesn't exist!");
				return;
			}
			if (sess == null || sess.getInstance() instanceof LobbyInstance) {
				Util.displayError(p, "You must be in a session to receive a holder artifact!");
				return;
			}
			PlayerSessionData psd = sess.getParty().get(p.getUniqueId());
			psd.giveArtifact(new ArtifactInstance((Artifact) eq, 1, new SessionEquipment(held)));
			return;
		}

		if (sess == null || sess.getInstance() instanceof LobbyInstance) {
			p.getInventory().addItem(eq.getItem());
		}
		else {
			sess.getParty().get(p.getUniqueId()).giveEquipment(eq);
		}
	}
}
