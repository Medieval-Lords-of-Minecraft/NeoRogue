package me.neoblade298.neorogue.commands;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Location;
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
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.map.MapPiece;
import me.neoblade298.neorogue.map.MapPieceInstance;

public class CmdAdminPieceSettings extends Subcommand {

	public CmdAdminPieceSettings(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		this.enableTabComplete();
		args.add(new Arg("Piece").setTabOptions(new ArrayList<String>(Map.getAllPieces().keySet())),
				new Arg("Rotations", false), new Arg("FlipX", false), new Arg("FlipY", false));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		HashMap<String, MapPiece> pieces = Map.getAllPieces();
		if (!pieces.containsKey(args[0])) {
			Util.displayError(p, "Couldn't find a map piece with that name!");
			return;
		}
		MapPiece piece = pieces.get(args[0]);
		MapPieceInstance inst = piece.getInstance();
		boolean pasteAll = args.length == 1;
		
		Location loc = p.getLocation();
		World w = p.getWorld();
		int x = loc.getBlockX(), z = loc.getBlockZ();
		x = -(x - (x % 16));
		z -= z % 16;
		final int PADDING = (Math.max(piece.getShape().getBaseHeight(), piece.getShape().getBaseLength()) + 1) * 16;
		
		// First clear the board
		try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(w))) {
		    CuboidRegion o = new CuboidRegion(
		    		BlockVector3.at(-x + PADDING, -63, z - PADDING),
		    		BlockVector3.at(-x - PADDING * (pasteAll ? 4 : 1), 64, z + PADDING * (pasteAll ? 4 : 1)));
		    Mask mask = new ExistingBlockMask(editSession);
		    try {
			    editSession.replaceBlocks(o, mask, BukkitAdapter.adapt(Material.AIR.createBlockData()));
			} catch (WorldEditException e) {
				e.printStackTrace();
			}
		}
		
		if (pasteAll) {
			for (int i = 0; i < 4; i++) {
				inst.setRotations(i);
				inst.setFlip(false, false);
				inst.testPaste(p, w, x + (PADDING * i), z);
				inst.setFlip(true, false);
				inst.testPaste(p, w, x + (PADDING * i), z + PADDING);
				inst.setFlip(false, true);
				inst.testPaste(p, w, x + (PADDING * i), z + (PADDING * 2));
			}
		}
		else {
			int rotations = Integer.parseInt(args[1]);
			boolean flipX = args.length > 2 ? args[2].equals("1") : false;
			boolean flipZ = args.length > 3 ? args[3].equals("1") : false;
			inst.setRotations(rotations);
			inst.setFlip(flipX, flipZ);
			inst.testPaste(p, w, x, z);
		}
		Util.msg(p, "Successfully pasted piece settings");
	}
}
