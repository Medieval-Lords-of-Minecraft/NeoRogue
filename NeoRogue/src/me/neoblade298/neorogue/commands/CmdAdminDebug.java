package me.neoblade298.neorogue.commands;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;

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
		World w = p.getWorld();
		
		if (!w.getName().equals("TestMap")) {
			Util.displayError(p, "You can't use this command on worlds that aren't named TestMap!");
			return;
		}
		
		if (piece == null) {
			Util.displayError(p, "Couldn't find a map piece with that name!");
			return;
		}
		
		MapPieceInstance inst = piece.getInstance();
		inst.setRotations(Integer.parseInt(args[3 + offset]));
		inst.setFlip(Integer.parseInt(args[1 + offset]) == 1, Integer.parseInt(args[2 + offset]) == 1);
		int x = 0;
		int z = 16;
		

		final int PADDING = (Math.max(piece.getShape().getBaseHeight(), piece.getShape().getBaseLength()) + 1) * 16;
		
		// First clear the board
		try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(w))) {
		    CuboidRegion o = new CuboidRegion(
		    		BlockVector3.at(-x + PADDING, 1, z - PADDING),
		    		BlockVector3.at(-x - PADDING, 128, z + PADDING));
		    Mask mask = new ExistingBlockMask(editSession);
		    try {
			    editSession.replaceBlocks(o, mask, BukkitAdapter.adapt(Material.AIR.createBlockData()));
			} catch (WorldEditException e) {
				e.printStackTrace();
			}
		}
		
		inst.testPaste(w, x, z);
	}
}
