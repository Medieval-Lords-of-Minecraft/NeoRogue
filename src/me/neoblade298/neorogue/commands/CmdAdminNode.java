package me.neoblade298.neorogue.commands;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.region.Node;
import me.neoblade298.neorogue.session.NodeSelectInstance;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;

public class CmdAdminNode extends Subcommand {

	public CmdAdminNode(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		this.enableTabComplete();
		args.add(new Arg("player", false), new Arg("row (0-15)"), new Arg("lane (0-4)"));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		int rowArg = 0;
		int laneArg = 1;
		
		// Check if first argument is a player name (not a number)
		if (args.length >= 3) {
			Player target = Bukkit.getPlayer(args[0]);
			if (target != null) {
				p = target;
				rowArg = 1;
				laneArg = 2;
			}
		}
		
		Session sess = SessionManager.getSession(p);
		
		if (sess == null) {
			Util.msg(s, p.getName() + " is not in a session!");
			return;
		}
		
		if (sess.getRegion() == null) {
			Util.msg(s, "No region has been generated yet!");
			return;
		}
		
		if (args.length < laneArg + 1) {
			Util.msg(s, "Usage: /nradmin setnode [player] <row> <lane>");
			return;
		}
		
		int row, lane;
		try {
			row = Integer.parseInt(args[rowArg]);
			lane = Integer.parseInt(args[laneArg]);
		} catch (NumberFormatException e) {
			Util.msg(s, "Row and lane must be numbers!");
			return;
		}
		
		Node[][] nodes = sess.getRegion().getNodes();
		
		if (row < 0 || row >= nodes.length) {
			Util.msg(s, "Invalid row! Must be between 0 and " + (nodes.length - 1));
			return;
		}
		
		if (lane < 0 || lane >= nodes[row].length) {
			Util.msg(s, "Invalid lane! Must be between 0 and " + (nodes[row].length - 1));
			return;
		}
		
		Node node = nodes[row][lane];
		if (node == null) {
			Util.msg(s, "No node exists at [" + row + "][" + lane + "]!");
			return;
		}
		
		Util.msg(s, "Setting node...");
		sess.getInstance().cleanup(false);
		String name = p.getName();

		new BukkitRunnable() {
			public void run() {
				sess.setNode(node);
				sess.getInstance().setup();
				sess.getRegion().update(node, (NodeSelectInstance) sess.getInstance());
				Util.msg(s, "Set " + name + "'s node to [" + row + "][" + lane + "]");

				try (Connection con = NeoCore.getConnection("NeoRogue-SessionManager");
						Statement insert = con.createStatement();
						Statement delete = con.createStatement()) {
					sess.save(insert, delete);
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
		}.runTaskLater(NeoRogue.inst(), 30L);
	}
}
