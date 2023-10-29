package me.neoblade298.neorogue.commands;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.map.MapPiece;
import me.neoblade298.neorogue.map.MapPieceInstance;

public class CmdAdminDebug extends Subcommand {

	public CmdAdminDebug(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("Piece"), new Arg("Area", false), new Arg("FlipX"), new Arg("FlipY"), new Arg("Rotations"));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		int offset = args.length - 4;
		LinkedList<MapPiece> pieces = Map.getPieces(offset == 1 ? AreaType.valueOf(args[1]) : AreaType.LOW_DISTRICT);
		MapPiece piece = null;
		for (MapPiece temp : pieces) {
			if (temp.getId().equalsIgnoreCase(args[0])) {
				piece = temp;
				break;
			}
		}
		Player p = (Player) s;
		Location loc = p.getLocation();
		World w = p.getWorld();
		int x = loc.getBlockX(), z = loc.getBlockZ();
		x = -(x + 16 - (x % 16));
		z -= z % 16;
		
		if (piece == null) {
			Util.displayError(p, "Couldn't find a map piece with that name!");
			return;
		}
		
		MapPieceInstance inst = piece.getInstance();
		inst.setRotations(Integer.parseInt(args[3 + offset]));
		inst.setFlip(Boolean.parseBoolean(args[1 + offset]), Boolean.parseBoolean(args[2 + offset]));
		inst.testPaste(w, x, z);
	}
}
