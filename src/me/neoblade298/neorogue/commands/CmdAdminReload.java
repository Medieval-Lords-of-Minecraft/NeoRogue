package me.neoblade298.neorogue.commands;

import org.bukkit.command.CommandSender;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.NeoRogue;

public class CmdAdminReload extends Subcommand {

	public CmdAdminReload(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
	}

	@Override
	public void run(CommandSender s, String[] args) {
		// Run git pull in mappieces directory
		org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(me.neoblade298.neorogue.NeoRogue.inst(), () -> {
			try {
				ProcessBuilder pb = new ProcessBuilder("git", "pull");
				pb.directory(new java.io.File("/home/mlmc/dev/plugins/NeoRogue/mappieces"));
				pb.redirectErrorStream(true);
				Process proc = pb.start();
				java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(proc.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					final String msg = line;
					org.bukkit.Bukkit.getScheduler().runTask(me.neoblade298.neorogue.NeoRogue.inst(), () -> Util.msg(s, msg));
				}
				int exitCode = proc.waitFor();
				org.bukkit.Bukkit.getScheduler().runTask(me.neoblade298.neorogue.NeoRogue.inst(), () -> {
					Util.msg(s, "git pull finished (exit code " + exitCode + ")");
					NeoRogue.reload();
					Util.msg(s, "Reloaded configurations.");
				});
			}
			catch (Exception e) {
				org.bukkit.Bukkit.getScheduler().runTask(me.neoblade298.neorogue.NeoRogue.inst(), () -> {
					Util.msg(s, "git pull failed: " + e.getMessage());
					NeoRogue.reload();
					Util.msg(s, "Reloaded configurations.");
				});
			}
		});
	}
}
