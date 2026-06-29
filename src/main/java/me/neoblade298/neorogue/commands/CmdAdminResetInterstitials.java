package me.neoblade298.neorogue.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.region.Region;
import me.neoblade298.neorogue.session.Plot;

public class CmdAdminResetInterstitials extends Subcommand {
	public CmdAdminResetInterstitials(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
	}

	@Override
	public void run(CommandSender s, String[] args) {
		World world = Bukkit.getWorld(Region.WORLD_NAME);
		if (world == null) {
			Util.msg(s, "<red>Could not find world " + Region.WORLD_NAME + ".");
			return;
		}

		int cleared = 0;
		for (int x = 0; x < 6 && cleared < 6; x++) {
			for (int z = 0; z < 100 && cleared < 6; z++) {
				Plot plot = new Plot(x, z);
				Location loc = new Location(world, -(plot.getXOffset() + 1), 62, plot.getZOffset());
				loc.getBlock().setType(Material.AIR);
				cleared++;
			}
		}

		Util.msg(s, "Reset interstitial version checks for first " + cleared + " plots.");
	}
}