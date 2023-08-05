package me.neoblade298.neorogue.map;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.shared.exceptions.NeoIOException;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.session.fights.FightInstance;

public class Map {
	private static HashMap<AreaType, LinkedList<MapPiece>> allPieces = new HashMap<AreaType, LinkedList<MapPiece>>(),
			usedPieces = new HashMap<AreaType, LinkedList<MapPiece>>();
	
	private ArrayList<MapPieceInstance> pieces = new ArrayList<MapPieceInstance>();
	private LinkedList<RotatableCoordinates> entrances = new LinkedList<RotatableCoordinates>();
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
			// TODO Collections.shuffle(allPieces.get(type));
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
			
			map.place(piece);
		}
		
		return map;
	}
	
	public boolean place(MapPiece piece) {
		// Special case, first piece being placed
		if (pieces.size() == 0) {
			MapPieceInstance inst = piece.getInstance();
			// Randomly rotate the piece TODO
			inst.rotate(NeoCore.gen.nextInt(4));
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
			System.out.println("Placing 1: " + piece.getId() + " with settings: " + inst.getNumRotations() + " " + inst.flipX + " " + inst.flipY);
			place(inst);
		}
		// Standard case, find an existing entrance and try to put the piece on
		else {
			TreeSet<MapPieceInstance> potentialPlacements = new TreeSet<MapPieceInstance>();
			for (RotatableCoordinates available : entrances) {
				for (RotatableCoordinates potential : piece.getEntrances()) {
					for (MapPieceInstance pSettings : piece.getRotationOptions(available, potential)) {
						int[] offset = pSettings.calculateOffset(available);
						if (canPlace(piece.getShape(), offset[0], offset[2])) {
							int value = Math.abs(offset[0] - (MAP_SIZE / 2)) + Math.abs(offset[2] - (MAP_SIZE / 2));
							pSettings.setPotential(value);
							potentialPlacements.add(pSettings);
							pSettings.setX(offset[0]);
							pSettings.setY(offset[1]);
							pSettings.setZ(offset[2]);
							System.out.println("Placeable," + value + ", settings " + pSettings.numRotations + " " + pSettings.flipX + " " + pSettings.flipY);
						}
					}
				}
				
				// If there are already plenty of options, don't try more
				if (potentialPlacements.size() > 10) break;
			}
			
			if (potentialPlacements.size() == 0) return false;

			MapPieceInstance best = potentialPlacements.first();
			System.out.println("Placing 2: " + piece.getId() + " with settings: " + best.numRotations + " " + best.flipX + " " + best.flipY);
			place(best);
		}
		display();
		return true;
	}
	
	private boolean canPlace(MapShape shape, int x, int z) {
		for (int i = 0; i < shape.getLength(); i++) {
			for (int j = 0; j < shape.getHeight(); j++) {
				if (this.shape[z + j][x + i]) return false;
			}
		}
		return true;
	}
	
	private MapPieceInstance selectBestSettings(HashMap<MapPieceInstance, Integer> placements) {
		int best = 999;
		MapPieceInstance s = null;
		boolean randomizer = NeoCore.gen.nextBoolean();
		for (Entry<MapPieceInstance, Integer> e : placements.entrySet()) {
			if (randomizer) {
				if (e.getValue() < best) {
					best = e.getValue();
					s = e.getKey();
				}
			}
			else {
				if (e.getValue() <= best) {
					best = e.getValue();
					s = e.getKey();
				}
			}
		}
		return s;
	}
	
	private void place(MapPieceInstance inst) {
		MapShape shape = inst.getPiece().getShape();
		for (int i = 0; i < shape.getLength(); i++) {
			for (int j = 0; j < shape.getHeight(); j++) {
				this.shape[inst.getX() + j][inst.getZ() + i] = shape.get(j, i);
			}
		}
		
		for (RotatableCoordinates entrance : inst.getPiece().getEntrances()) {
			RotatableCoordinates coords = entrance.clone();
			coords.applySettings(inst);
			entrances.add(coords);
		}
		
		pieces.add(inst);
	}
	
	public void instantiate(FightInstance fi, int xOff, int zOff) {
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
		for (int i = shape.length - 1; i >= 0; i--) {
			System.out.print(i + ": ");
			for (int j = 0; j < shape[0].length; j++) {
				System.out.print(shape[i][j] ? "X" : "_");
			}
			System.out.println();
		}
	}
}
