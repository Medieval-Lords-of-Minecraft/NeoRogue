package me.neoblade298.neorogue.commands;

import org.bukkit.command.CommandSender;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.NeoRogue;

public class CmdAdminReload extends Subcommand {

	public CmdAdminReload(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("fast", false));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		boolean fast = args.length > 0 && args[0].equalsIgnoreCase("fast");
		if (fast) {
			NeoRogue.reload();
			Util.msgRaw(s, "Reloaded configurations (fast, no git pull).");
		} else {
			org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(me.neoblade298.neorogue.NeoRogue.inst(), () -> {
				gitPull(s, "/home/mlmc/dev/plugins/NeoRogue/mappieces");
				gitPull(s, "/home/mlmc/dev/plugins/MythicMobs");
				org.bukkit.Bukkit.getScheduler().runTask(me.neoblade298.neorogue.NeoRogue.inst(), () -> {
					NeoRogue.reload();
					Util.msgRaw(s, "Reloaded configurations.");
				});
			});
		}
	}

	private void gitPull(CommandSender s, String directory) {
		try {
			Util.msgRaw(s, "Running git pull in " + directory);
			ProcessBuilder pb = new ProcessBuilder("git", "pull");
			pb.directory(new java.io.File(directory));
			pb.redirectErrorStream(true);
			Process proc = pb.start();
			java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(proc.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				final String msg = line;
				org.bukkit.Bukkit.getScheduler().runTask(me.neoblade298.neorogue.NeoRogue.inst(), () -> Util.msgRaw(s, msg));
			}
			int exitCode = proc.waitFor();
			final String dir = directory;
			org.bukkit.Bukkit.getScheduler().runTask(me.neoblade298.neorogue.NeoRogue.inst(), () -> {
				Util.msgRaw(s, "git pull in " + dir + " finished (exit code " + exitCode + ")");
			});
		}
		catch (Exception e) {
			org.bukkit.Bukkit.getScheduler().runTask(me.neoblade298.neorogue.NeoRogue.inst(), () -> {
				Util.msgRaw(s, "git pull in " + directory + " failed: " + e.getMessage());
			});
		}
	}
}
