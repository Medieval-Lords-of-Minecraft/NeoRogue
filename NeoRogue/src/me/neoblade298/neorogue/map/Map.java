package me.neoblade298.neorogue.map;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.shared.exceptions.NeoIOException;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.session.fights.FightInstance;

public class Map {
	private static HashMap<AreaType, LinkedList<MapPiece>> allPieces = new HashMap<AreaType, LinkedList<MapPiece>>(),
			usedPieces = new HashMap<AreaType, LinkedList<MapPiece>>();
	
	private ArrayList<MapPieceInstance> pieces = new ArrayList<MapPieceInstance>();
	private LinkedList<Coordinates> entrances = new LinkedList<Coordinates>();
	private LinkedList<Coordinates> blockedEntrances = new LinkedList<Coordinates>();
	private static final int MAP_SIZE = 12;
	private boolean[][] shape = new boolean[MAP_SIZE][MAP_SIZE];
	
	public static void load() {
		for (AreaType type : AreaType.values()) {
			allPieces.put(type, new LinkedList<MapPiece>());
			usedPieces.put(type, new LinkedList<MapPiece>());
		}
		
		try {
			NeoCore.loadFiles(new File(NeoRogue.inst().getDataFolder(), "mappieces"), (yml, file) -> {
				for (String key : yml.getKeys(false)) {
					ConfigurationSection sec = yml.getConfigurationSection(key);
					MapPiece piece = new MapPiece(sec);
					allPieces.get(AreaType.valueOf(sec.getString("type"))).add(piece);
				}
			});
		} catch (NeoIOException e) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to load MapPiece");
			e.printStackTrace();
		}
		
		for (AreaType type : AreaType.values()) {
			//Collections.shuffle(allPieces.get(type));
		}
	}
	
	public static Map generate(AreaType type, int numPieces) {
		Map map = new Map();
		LinkedList<MapPiece> pieces = allPieces.get(type);
		
		for (int i = 0; i < numPieces; i++) {
			MapPiece piece = null;
			do {
				if (pieces.size() == 0) shufflePieces(type);
				piece = pieces.poll();
			}
			// Make sure there are enough entrances to continue expanding while we still need size
			while (map.entrances.size() < 2 && piece.getNumEntrances() < 2 && i < numPieces - 1);
			
			if (piece == null) {
				Bukkit.getLogger().warning("[NeoRogue] Failed to find piece for generation. Returning map as is.");
				return map;
			}
			map.place(piece);
			
		}
		
		return map;
	}
	
	public boolean place(MapPiece piece) {
		// Special case, first piece being placed
		if (pieces.size() == 0) {
			MapPieceInstance inst = piece.getInstance();
			// Randomly rotate the piece TODO
			inst.setRotations(NeoCore.gen.nextInt(4));
			int rand = NeoCore.gen.nextInt(3);
			if (rand == 1) inst.flip(true);
			else if (rand == 2) inst.flip(false);
			MapShape shape = inst.getPiece().getShape();
			shape.applySettings(inst);
			
			// Place the piece in the middle of the board
			int x = (MAP_SIZE / 2) - (shape.getHeight() / 2);
			int z = (MAP_SIZE / 2) - (shape.getLength() / 2);
			
			inst.setX(x);
			inst.setY(0);
			inst.setZ(z);
			System.out.println("Placing 1: " + piece.getId() + " with settings: " + inst.getNumRotations() + " " + inst.flipX + " " + inst.flipZ);
			place(inst);
		}
		// Standard case, find an existing entrance and try to put the piece on
		else {
			TreeSet<MapPieceInstance> potentialPlacements = new TreeSet<MapPieceInstance>();
			for (Coordinates available : entrances) {
				for (Coordinates potential : piece.getEntrances()) {
					for (MapPieceInstance pSettings : piece.getRotationOptions(available, potential)) {
						piece.getShape().applySettings(pSettings);
						int[] offset = pSettings.calculateOffset(available);
						System.out.println("? " + pSettings.numRotations + " " + pSettings.flipX + " " + pSettings.flipZ + ": " + available.toStringFacing() + " -> " + pSettings.getEntrance());
						System.out.println("O: " + offset[0] + " " + offset[2]);
						if (canPlace(piece.getShape(), offset[0], offset[2])) {
							int value = Math.abs(offset[0] - (MAP_SIZE / 2)) + Math.abs(offset[2] - (MAP_SIZE / 2));
							pSettings.setPotential(value);
							potentialPlacements.add(pSettings);
							pSettings.setX(offset[0]);
							pSettings.setY(offset[1]);
							pSettings.setZ(offset[2]);
							System.out.println("! " + value);
						}
					}
				}
				
				// If there are already plenty of options, don't try more
				if (potentialPlacements.size() > 50) break;
			}
			
			if (potentialPlacements.size() == 0) return false;

			MapPieceInstance best = potentialPlacements.first();
			System.out.println("Placing 2: " + piece.getId() + " with settings: " + best.numRotations + " " + best.flipX + " " + best.flipZ);
			place(best);
		}
		display();
		return true;
	}
	
	private boolean canPlace(MapShape shape, int x, int z) {
		for (int i = 0; i < shape.getLength(); i++) {
			for (int j = 0; j < shape.getHeight(); j++) {
				if (x + i > MAP_SIZE - 1 || x + i < 0 || z + j > MAP_SIZE - 1 || z + j < 0) return false;
				
				System.out.println(">> " + (x + i) + "," + (z + j) + "; " + i + "," + j + ": " + this.shape[x + i][z + j] + " " + shape.get(i, j));
				if (this.shape[x + i][z + j] && shape.get(i, j)) return false;
			}
		}
		return true;
	}
	
	private void place(MapPieceInstance inst) {
		MapShape shape = inst.getPiece().getShape();
		entrances.remove(inst.getAvailableEntrance());
		shape.applySettings(inst);
		System.out.println("Placing: ");
		for (int i = 0; i < shape.getLength(); i++) {
			for (int j = 0; j < shape.getHeight(); j++) {
				boolean b = shape.get(i, j);
				System.out.println((inst.getX() + i) + "," + (inst.getZ() + j) + ": " + i + "," + j + " - " + shape.get(i, j));
				
				// Only do things if we're placing a tangible piece
				if (b) {
					this.shape[inst.getX() + i][inst.getZ() + j] = shape.get(i, j);

					// Remove any entrances at this location, may need to save them to properly block them
					Iterator<Coordinates> iter = entrances.iterator();
					while (iter.hasNext()) {
						Coordinates entrance = iter.next();
						if (entrance.getXFacing() == inst.getX() + i && entrance.getZFacing() == inst.getZ() + j) {
							blockedEntrances.add(entrance);
							iter.remove();
						}
					}
				}
			}
		}
		
		for (Coordinates entrance : inst.getPiece().getEntrances()) {
			Coordinates coords = entrance.clone();
			coords.applySettings(inst);
			if (coords.getXFacing() > MAP_SIZE - 1 || coords.getXFacing() < 0 || coords.getZFacing() > MAP_SIZE - 1 || coords.getZFacing() < 0)
				continue;
			
			 // Don't add entrance if it's already blocked
			if (this.shape[coords.getXFacing()][coords.getZFacing()]) {
				blockedEntrances.add(entrance);
				continue;
			}
			entrances.add(coords);
		}
		
		pieces.add(inst);
	}
	
	public void cleanup() {
		
	}
	
	public void instantiate(FightInstance fi, int xOff, int zOff) {
		// First clear the board
		try (EditSession editSession = WorldEdit.getInstance().newEditSession(Area.world)) {
		    CuboidRegion o = new CuboidRegion(
		    		BlockVector3.at(-xOff, 0, MapPieceInstance.Z_FIGHT_OFFSET + zOff),
		    		BlockVector3.at(-(xOff + MAP_SIZE * 16), 128, MapPieceInstance.Z_FIGHT_OFFSET + zOff + MAP_SIZE * 16));
		    Mask mask = new ExistingBlockMask(editSession);
		    try {
			    editSession.replaceBlocks(o, mask, BukkitAdapter.adapt(Material.AIR.createBlockData()));
			} catch (WorldEditException e) {
				e.printStackTrace();
			}
		}
		
		for (MapPieceInstance inst : pieces) {
			inst.instantiate(fi, xOff, zOff);
		}
		
	}
	
	private static void shufflePieces(AreaType type) {
		Collections.shuffle(usedPieces.get(type));
		allPieces.get(type).addAll(usedPieces.get(type));
	}
	
	public static LinkedList<MapPiece> getPieces(AreaType type) {
		return allPieces.get(type);
	}
	
	public void display() {
		System.out.println("   0123456789AB");
		for (int z = shape[0].length - 1; z >= 0; z--) {
			System.out.print(z + ": ");
			for (int x = 0; x < shape.length; x++) {
				System.out.print(shape[x][z] ? "X" : ".");
			}
			System.out.println();
		}
	}
}