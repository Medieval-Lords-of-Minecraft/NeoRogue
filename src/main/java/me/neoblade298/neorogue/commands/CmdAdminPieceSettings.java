package me.neoblade298.neorogue.commands;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Rotatable;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.map.MapPiece;
import me.neoblade298.neorogue.map.MapPieceInstance;
import me.neoblade298.neorogue.region.Region;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

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
		boolean pasteAll = args.length == 1;

		int PADDING = (Math.max(piece.getShape().getBaseHeight(), piece.getShape().getBaseLength()) + 1) * 16;
		int clearMinY = MapPieceInstance.Y_OFFSET - 5;
		int clearMaxY = MapPieceInstance.Y_OFFSET + 40;
		int clearX = PADDING * (pasteAll ? 4 : 1);
		int clearZ = PADDING * (pasteAll ? 3 : 1);

		Region.useTestWorld();

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
		
		if (pasteAll) {
			for (int i = 0; i < 4; i++) {
				// Each variant needs its own instance: markSpawns places its markers ~20 ticks later
				// and reads the instance's rotation/flip at that time, so a shared instance would make
				// every variant's spawns use the last-applied settings.
				MapPieceInstance noFlip = piece.getInstance();
				noFlip.setRotations(i);
				noFlip.setFlip(false, false);
				noFlip.instantiate(null, PADDING * i, 0);
				noFlip.markSpawns(p, PADDING * i, 0);
				placeVariantSign(PADDING * i, 0, "rot=" + i, "no flip");

				MapPieceInstance flipX = piece.getInstance();
				flipX.setRotations(i);
				flipX.setFlip(true, false);
				flipX.instantiate(null, PADDING * i, PADDING);
				flipX.markSpawns(p, PADDING * i, PADDING);
				placeVariantSign(PADDING * i, PADDING, "rot=" + i, "flipX");

				MapPieceInstance flipZ = piece.getInstance();
				flipZ.setRotations(i);
				flipZ.setFlip(false, true);
				flipZ.instantiate(null, PADDING * i, PADDING * 2);
				flipZ.markSpawns(p, PADDING * i, PADDING * 2);
				placeVariantSign(PADDING * i, PADDING * 2, "rot=" + i, "flipZ");
			}
		}
		else {
			int rotations = Integer.parseInt(args[1]);
			boolean flipX = args.length > 2 ? args[2].equals("1") : false;
			boolean flipZ = args.length > 3 ? args[3].equals("1") : false;
			MapPieceInstance inst = piece.getInstance();
			inst.setRotations(rotations);
			inst.setFlip(flipX, flipZ);
			inst.instantiate(null, 0, 0);
			inst.markSpawns(p, 0, 0);
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				Region.useMainWorld();
				org.bukkit.World w = Bukkit.getWorld(Region.TEST_WORLD_NAME);
				p.teleport(new Location(w, -(MapPieceInstance.X_FIGHT_OFFSET), MapPieceInstance.Y_OFFSET + 1, MapPieceInstance.Z_FIGHT_OFFSET));
				Util.msgRaw(p, "Successfully pasted piece settings");
			}
		}.runTaskLater(NeoRogue.inst(), 20);
	}

	private void placeVariantSign(int xOff, int zOff, String line1, String line2) {
		org.bukkit.World w = Bukkit.getWorld(Region.getActiveWorldName());
		Location signLoc = new Location(w,
				-(xOff + MapPieceInstance.X_FIGHT_OFFSET),
				MapPieceInstance.Y_OFFSET + 2,
				MapPieceInstance.Z_FIGHT_OFFSET + zOff);
		Block signBlock = signLoc.getBlock();
		signBlock.setType(Material.OAK_SIGN);
		Rotatable signData = (Rotatable) signBlock.getBlockData();
		signData.setRotation(BlockFace.SOUTH);
		signBlock.setBlockData(signData);
		Sign sign = (Sign) signBlock.getState();
		sign.getSide(org.bukkit.block.sign.Side.FRONT).line(0, Component.text(line1).color(NamedTextColor.YELLOW));
		sign.getSide(org.bukkit.block.sign.Side.FRONT).line(1, Component.text(line2).color(NamedTextColor.WHITE));
		sign.update();
	}
}
