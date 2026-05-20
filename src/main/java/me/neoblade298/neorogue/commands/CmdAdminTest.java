package me.neoblade298.neorogue.commands;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.map.ProceduralMountainGenerator;
import me.neoblade298.neorogue.map.SpawnPointDetector;

public class CmdAdminTest extends Subcommand {

	public CmdAdminTest(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
	}

	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		
		// Parse arguments: /nradmin test [size] [scale] [height] [caves|decorations]
		int size = args.length > 0 ? Integer.parseInt(args[0]) : 64;
		double scale = args.length > 1 ? Double.parseDouble(args[1]) : 0.015;
		int height = args.length > 2 ? Integer.parseInt(args[2]) : 35;
		boolean caves = args.length > 3 && args[3].equalsIgnoreCase("caves");
		boolean decorations = args.length > 3 && args[3].equalsIgnoreCase("decorations");
		
		Location loc = p.getLocation();
		World world = loc.getWorld();
		int xOffset = loc.getBlockX();
		int zOffset = loc.getBlockZ();
		
		Util.msg(p, "&7Generating " + size + "x" + size + " mountain at your location...");
		Util.msg(p, "&7Scale: " + scale + ", Height: " + height + ", Caves: " + caves);
		
		// Generate the mountain
		long seed = NeoRogue.gen.nextLong();
		ProceduralMountainGenerator generator = new ProceduralMountainGenerator(seed);
		generator.setHeight(64, height)
				 .setScale(scale)
				 .setCaves(caves);
		
		generator.generate(world, xOffset, zOffset, size);
		
		// Add decorations if requested
		if (decorations) {
			Util.msg(p, "&7Adding decorations...");
			generator.addDecorations(world, xOffset, zOffset, size);
		}
		
		// Detect and mark spawn points
		Util.msg(p, "&7Detecting spawn points...");
		SpawnPointDetector detector = new SpawnPointDetector();
		detector.detectSpawnPoints(world, xOffset, xOffset + size, zOffset, zOffset + size);
		
		List<Location> playerSpawns = detector.getPlayerSpawns();
		List<Location> mobSpawns = detector.getMobSpawns();
		
		// Mark player spawns with emerald blocks
		for (Location spawn : playerSpawns) {
			Block block = spawn.getBlock();
			block.getRelative(0, -1, 0).setType(Material.EMERALD_BLOCK);
		}
		
		// Mark mob spawns with redstone blocks
		for (Location spawn : mobSpawns) {
			Block block = spawn.getBlock();
			block.getRelative(0, -1, 0).setType(Material.REDSTONE_BLOCK);
		}
		
		Util.msg(p, "&aGeneration complete!");
		Util.msg(p, "&7Player spawns: &a" + playerSpawns.size() + "&7 (emerald blocks)");
		Util.msg(p, "&7Mob spawns: &c" + mobSpawns.size() + "&7 (redstone blocks)");
		Util.msg(p, "&7Seed: &e" + seed);
	}
}
