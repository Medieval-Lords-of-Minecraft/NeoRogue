package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;

public class CmdAdminTest extends Subcommand {

	public CmdAdminTest(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		ArrayList<String> artifactIds = new ArrayList<String>();
		for (Equipment eq : Equipment.getAll()) {
			if (eq instanceof Artifact) artifactIds.add(eq.getId());
		}
		args.add(new Arg("artifact id", false).setTabOptions(artifactIds));
		this.enableTabComplete();
	}

	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		Session sess = SessionManager.getSession(p);
		if (sess == null) {
			Util.displayError(p, "You're not in a session!");
			return;
		}
		PlayerSessionData data = sess.getData(p.getUniqueId());
		if (args.length == 0) {
			System.out.println(data.getArtifactDroptable());
			Util.msg(p, data.getArtifactDroptable().toString());
		}
		else {
			data.getArtifactDroptable().remove((Artifact) Equipment.get(args[0], false));
		}
	}
}
