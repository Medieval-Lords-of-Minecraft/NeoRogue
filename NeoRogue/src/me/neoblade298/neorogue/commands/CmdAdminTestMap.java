package me.neoblade298.neorogue.commands;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.map.Coordinates;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.map.MapPiece;
import me.neoblade298.neorogue.map.MapPieceInstance;

public class CmdAdminTestMap extends Subcommand {

	public CmdAdminTestMap(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("AreaType", false), new Arg("RequiredPiece", false), new Arg("NumPieces", false));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		int numPieces = args.length > 2 ? Integer.parseInt(args[2]) : 5;
		AreaType type = args.length > 0 ? AreaType.valueOf(args[0]) : AreaType.LOW_DISTRICT;
		LinkedList<MapPiece> pieces = Map.getPieces(type);
		Player p = (Player) s;

		MapPiece piece = null;
		if (args.length > 1) {
			for (MapPiece temp : pieces) {
				if (temp.getId().equalsIgnoreCase(args[0])) {
					piece = temp;
					break;
				}
			}
			
			if (piece == null) {
				Util.displayError(p, "Couldn't find a map piece with that name!");
				return;
			}
		}
		
		
		Map map = piece == null ? Map.generate(type, numPieces) : Map.generate(type, numPieces, piece);
		Util.msg(p, "Successfully generated map");
		int xOff = 0, zOff = 0;
		map.instantiateBlocks(xOff, zOff);
		
		// Choose random teleport location
		int rand = NeoCore.gen.nextInt(map.getPieces().size());
		MapPieceInstance inst = map.getPieces().get(rand);
		Coordinates[] spawns = inst.getSpawns();
		Location spawn = spawns[spawns.length > 1 ? NeoCore.gen.nextInt(spawns.length) : 0].clone().applySettings(inst).toLocation();
		spawn.add(xOff + MapPieceInstance.X_FIGHT_OFFSET,
				MapPieceInstance.Y_OFFSET,
				MapPieceInstance.Z_FIGHT_OFFSET + zOff);
		spawn.setX(-spawn.getX());
		p.teleport(spawn);
	}
}