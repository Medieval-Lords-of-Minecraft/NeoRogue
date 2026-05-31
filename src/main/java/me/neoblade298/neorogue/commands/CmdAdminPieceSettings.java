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

		int PADDING = (Math.max(piece.getShape().getBaseHeight(), piece.getShape().getBaseLength()) + 1) * 16;
		int clearMinY = MapPieceInstance.Y_OFFSET - 5;
		int clearMaxY = MapPieceInstance.Y_OFFSET + 40;
		int clearX = PADDING * (pasteAll ? 4 : 1);
		int clearZ = PADDING * (pasteAll ? 3 : 1);

		// Clear the area
		try (EditSession editSession = WorldEdit.getInstance().newEditSession(Region.world)) {
			CuboidRegion r = new CuboidRegion(
					BlockVector3.at(-(MapPieceInstance.X_FIGHT_OFFSET) + PADDING, clearMinY, MapPieceInstance.Z_FIGHT_OFFSET - PADDING),
					BlockVector3.at(-(MapPieceInstance.X_FIGHT_OFFSET) - clearX, clearMaxY, MapPieceInstance.Z_FIGHT_OFFSET + clearZ));
			try {
				editSession.replaceBlocks(r, new ExistingBlockMask(editSession), BukkitAdapter.adapt(Material.AIR.createBlockData()));
			} catch (WorldEditException e) {
				e.printStackTrace();
			}
		}

		ArrayList<Location> potentialSpawns = new ArrayList<>();
		if (pasteAll) {
			for (int i = 0; i < 4; i++) {
				inst.setRotations(i);
				inst.setFlip(false, false);
				inst.instantiate(null, PADDING * i, 0);
				potentialSpawns.addAll(inst.markSpawns(p, PADDING * i, 0, 1));

				inst.setFlip(true, false);
				inst.instantiate(null, PADDING * i, PADDING);
				potentialSpawns.addAll(inst.markSpawns(p, PADDING * i, PADDING, 1));

				inst.setFlip(false, true);
				inst.instantiate(null, PADDING * i, PADDING * 2);
				potentialSpawns.addAll(inst.markSpawns(p, PADDING * i, PADDING * 2, 1));
			}
		}
		else {
			int rotations = Integer.parseInt(args[1]);
			boolean flipX = args.length > 2 ? args[2].equals("1") : false;
			boolean flipZ = args.length > 3 ? args[3].equals("1") : false;
			inst.setRotations(rotations);
			inst.setFlip(flipX, flipZ);
			inst.instantiate(null, 0, 0);
			potentialSpawns.addAll(inst.markSpawns(p, 0, 0, 1));
		}

		if (!potentialSpawns.isEmpty()) {
			p.teleport(potentialSpawns.get(0));
		} else {
			org.bukkit.World w = Bukkit.getWorld(Region.WORLD_NAME);
			p.teleport(new Location(w, -(MapPieceInstance.X_FIGHT_OFFSET), MapPieceInstance.Y_OFFSET + 1, MapPieceInstance.Z_FIGHT_OFFSET));
		}
		Util.msg(p, "Successfully pasted piece settings");
	}
}
