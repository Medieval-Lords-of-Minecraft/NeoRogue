package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.map.MapPiece;
import me.neoblade298.neorogue.map.MapPieceInstance;
import me.neoblade298.neorogue.region.Region;
import me.neoblade298.neorogue.region.RegionType;

public class CmdAdminMap extends Subcommand {
	public CmdAdminMap(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		this.enableTabComplete();
		ArrayList<String> tab = new ArrayList<String>();
		for (RegionType type : RegionType.values()) {
			tab.add(type.toString());
		}
		args.add(new Arg("RegionType", false).setTabOptions(tab), new Arg("NumPieces", false), new Arg("RequiredPiece", false));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		int numPieces = args.length > 1 ? Integer.parseInt(args[1]) : 5;
		RegionType type = args.length > 0 ? RegionType.valueOf(args[0]) : RegionType.LOW_DISTRICT;

		MapPiece piece = null;
		if (args.length > 2) {
			piece = Map.getAllPieces().get(args[2]);
			
			if (piece == null) {
				Util.displayError(p, "Couldn't find a map piece with that name!");
				return;
			}
		}
		
		if (Map.getPieces(type) == null || Map.getPieces(type).isEmpty()) {
			Util.displayError(p, "No map pieces loaded for region " + type + "!");
			return;
		}
		
		Region.useTestWorld();

		long startTime = System.currentTimeMillis();
		Map map = piece == null ? Map.generate(type, numPieces, false) : Map.generate(type, numPieces, piece, false);
		long genTime = System.currentTimeMillis() - startTime;
		Util.msg(p, "Piece generation: " + genTime + "ms");
		long instantiateStart = System.currentTimeMillis();
		int xOff = 0, zOff = 0;
		map.instantiate(null, xOff, zOff);
		long instantiateTime = System.currentTimeMillis() - instantiateStart;
		Util.msg(p, "Instantiate (clear + schedule): " + instantiateTime + "ms");
		long totalTime = System.currentTimeMillis() - startTime;
		Util.msg(p, "Total sync time: " + totalTime + "ms (terrain gen runs async)");
		map.display();

		// List each map piece instance
		int idx = 0;
		for (MapPieceInstance mpi : map.getPieces()) {
			Util.msg(p, "#" + idx + ": " + mpi.getPiece().getId()
					+ " rot=" + mpi.getNumRotations()
					+ " flipX=" + mpi.isFlipX()
					+ " flipZ=" + mpi.isFlipZ());
			idx++;
		}
		
		// Mark down spawn location blocks
		ArrayList<Location> potentialSpawns = new ArrayList<Location>();
		int ws = map.getWorldStride();
		for (MapPieceInstance mpi : map.getPieces()) {
			potentialSpawns.addAll(mpi.markSpawns(0, 0, ws));
		}

		Region.useMainWorld();
		
		// Choose random teleport location
		int rand = NeoRogue.gen.nextInt(potentialSpawns.size());
		p.teleport(potentialSpawns.get(rand));
	}
}
