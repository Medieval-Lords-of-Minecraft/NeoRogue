package me.neoblade298.neorogue.commands;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.map.MapPiece;
import me.neoblade298.neorogue.map.MapPieceInstance;
import me.neoblade298.neorogue.region.Region;

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
		
		MapPieceInstance inst = piece.getInstance();
		int xOff = 0, zOff = 0;

		// Clear the area
		int clearPadding = (Math.max(piece.getShape().getBaseHeight(), piece.getShape().getBaseLength()) + 1) * 16;
		int clearMinY = MapPieceInstance.Y_OFFSET - 5;
		int clearMaxY = MapPieceInstance.Y_OFFSET + 40;
		try (EditSession editSession = WorldEdit.getInstance().newEditSession(Region.world)) {
			CuboidRegion r = new CuboidRegion(
					BlockVector3.at(-(xOff + MapPieceInstance.X_FIGHT_OFFSET) + clearPadding, clearMinY, MapPieceInstance.Z_FIGHT_OFFSET + zOff - clearPadding),
					BlockVector3.at(-(xOff + MapPieceInstance.X_FIGHT_OFFSET) - clearPadding, clearMaxY, MapPieceInstance.Z_FIGHT_OFFSET + zOff + clearPadding));
			try {
				editSession.replaceBlocks(r, new ExistingBlockMask(editSession), BukkitAdapter.adapt(Material.AIR.createBlockData()));
			} catch (WorldEditException e) {
				e.printStackTrace();
			}
		}

		// Paste the piece using the same method as /nra map
		inst.instantiate(null, xOff, zOff);

		// Mark spawn locations with terracotta
		ArrayList<Location> potentialSpawns = inst.markSpawns(xOff, zOff);

		if (!potentialSpawns.isEmpty()) {
			p.teleport(potentialSpawns.get(0));
		} else {
			// Teleport to the fight offset origin
			org.bukkit.World w = Bukkit.getWorld(Region.WORLD_NAME);
			p.teleport(new Location(w, -(xOff + MapPieceInstance.X_FIGHT_OFFSET), MapPieceInstance.Y_OFFSET + 1, MapPieceInstance.Z_FIGHT_OFFSET + zOff));
		}
	}
}
