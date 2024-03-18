package me.neoblade298.neorogue.commands;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
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

public class CmdAdminPiece extends Subcommand {

	public CmdAdminPiece(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		this.enableTabComplete();
		args.add(new Arg("Piece").setTabOptions(new ArrayList<String>(Map.getAllPieces().keySet())));
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
		World w = Bukkit.getWorld("TestMap");
		
		if (w == null) {
			Util.displayError(p, "There must be a loaded world named TestMap!");
			return;
		}
		
		MapPieceInstance inst = piece.getInstance();
		inst.setRotations(2);
		inst.setFlip(true, false);
		int x = -(piece.getShape().getBaseLength() * 16);
		int z = 16;
		final int PADDING = (Math.max(piece.getShape().getBaseHeight(), piece.getShape().getBaseLength()) + 1) * 16;
		
		// First clear the board
		try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(w))) {
		    CuboidRegion o = new CuboidRegion(
		    		BlockVector3.at(-x + PADDING, -63, z - PADDING),
		    		BlockVector3.at(-x - PADDING, 64, z + PADDING));
		    Mask mask = new ExistingBlockMask(editSession);
		    try {
			    editSession.replaceBlocks(o, mask, BukkitAdapter.adapt(Material.AIR.createBlockData()));
			} catch (WorldEditException e) {
				e.printStackTrace();
			}
		}
		
		inst.testPaste(p, w, x, z);
		p.teleport(new Location(w, -8, 1, 8, -90, 0));
	}
}
