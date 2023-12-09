package me.neoblade298.neorogue.map;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.shared.io.Section;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.Mob;
import me.neoblade298.neorogue.session.fight.MobModifier;

public class Map {
	private static HashMap<AreaType, LinkedList<MapPiece>> standardPieces = new HashMap<AreaType, LinkedList<MapPiece>>(),
			usedPieces = new HashMap<AreaType, LinkedList<MapPiece>>();
	private static HashMap<AreaType, ArrayList<MapPiece>> minibossPieces = new HashMap<AreaType, ArrayList<MapPiece>>(),
			bossPieces = new HashMap<AreaType, ArrayList<MapPiece>>();
	private static final int MAP_SIZE = 12;
	
	private ArrayList<MapPieceInstance> pieces = new ArrayList<MapPieceInstance>();
	private LinkedList<Coordinates> entrances = new LinkedList<Coordinates>(),
			blockedEntrances = new LinkedList<Coordinates>();
	private TreeMap<Mob, ArrayList<MobModifier>> mobs = new TreeMap<Mob, ArrayList<MobModifier>>();
	private HashSet<String> targets = new HashSet<String>();
	private boolean[][] shape = new boolean[MAP_SIZE][MAP_SIZE];
	
	public static void load() {
		for (AreaType type : AreaType.values()) {
			standardPieces.put(type, new LinkedList<MapPiece>());
			usedPieces.put(type, new LinkedList<MapPiece>());
			minibossPieces.put(type, new ArrayList<MapPiece>());
			bossPieces.put(type, new ArrayList<MapPiece>());
		}
		
		NeoCore.loadFiles(new File(NeoRogue.inst().getDataFolder(), "mappieces"), (yml, file) -> {
			for (String key : yml.getKeys()) {
				try {
					Section sec = yml.getSection(key);
					MapPiece piece = new MapPiece(sec);
					AreaType area = AreaType.valueOf(sec.getString("area"));
					String type = sec.getString("type", "STANDARD");
					
					if (type.equals("BOSS")) bossPieces.get(area).add(piece);
					else if (type.equals("MINIBOSS")) minibossPieces.get(area).add(piece);
					else standardPieces.get(area).add(piece);
				}
				catch (Exception e) {
					e.printStackTrace();
					Bukkit.getLogger().warning("[NeoRogue] Failed to load MapPiece " + key + " in file " + file.getName());
				}
			}
		});
		
		for (AreaType type : AreaType.values()) {
			Collections.shuffle(standardPieces.get(type));
		}
	}
	
	public static Map generateBoss(AreaType type, int numPieces) {
		MapPiece piece = bossPieces.get(type).get(NeoCore.gen.nextInt(bossPieces.get(type).size()));
		return generate(type, numPieces, piece);
	}
	
	public static Map generateMiniboss(AreaType type, int numPieces) {
		MapPiece piece = minibossPieces.get(type).get(NeoCore.gen.nextInt(minibossPieces.get(type).size()));
		return generate(type, numPieces, piece);
	}
	
	public static Map generate(AreaType type, int numPieces) {
		return generate(new Map(), type, numPieces);
	}
	
	public static Map generate(AreaType type, int numPieces, MapPiece requiredPiece) {
		Map map = new Map();
		map.place(requiredPiece);
		return generate(map, type, numPieces);
	}
	
	public static Map generate(Map map, AreaType type, int numPieces) {
		LinkedList<MapPiece> pieces = standardPieces.get(type);
		int totalAttempts = pieces.size() + usedPieces.get(type).size();
		
		for (int i = 0; i < numPieces; i++) {
			MapPiece piece = null;
			int attempts = 0;
			do {
				if (pieces.size() == 0) shufflePieces(type);
				piece = pieces.poll();
				usedPieces.get(type).add(piece);
				
				if (attempts++ > totalAttempts) {
					Bukkit.getLogger().warning("[NeoRogue] Ran out of valid pieces to place. Returning map as is.");
					return map;
				}
			}
			/* Skip to another piece if
			* 1: we have 1 entrance left and the next piece is 1 entrance and there are still more pieces we need to place
			* 2: the piece cannot be placed anywhere on the map
			*/
			while ((map.entrances.size() < 2 && piece.getNumEntrances() < 2 && i < numPieces - 1)
					|| !map.place(piece));

			
			if (piece == null) {
				Bukkit.getLogger().warning("[NeoRogue] Failed to find piece for generation. Returning map as is.");
				return map;
			}
		}
		
		return map;
	}
	
	public boolean place(MapPiece piece) {
		// Special case, first piece being placed
		if (pieces.size() == 0) {
			MapPieceInstance inst = piece.getInstance();
			// Randomly rotate the piece
			inst.setRotations(NeoCore.gen.nextInt(4));
			int rand = NeoCore.gen.nextInt(3);
			if (rand == 1) inst.setFlip(true, false);
			else if (rand == 2) inst.setFlip(false, true);
			MapShape shape = inst.getPiece().getShape();
			shape.applySettings(inst);
			
			// Place the piece in the middle of the board
			int x = (MAP_SIZE / 2) - (shape.getHeight() / 2);
			int z = (MAP_SIZE / 2) - (shape.getLength() / 2);
			
			inst.setX(x);
			inst.setY(0);
			inst.setZ(z);
			place(inst, false);
		}
		// Standard case, find an existing entrance and try to put the piece on
		else {
			TreeSet<MapPieceInstance> potentialPlacements = new TreeSet<MapPieceInstance>();
			for (Coordinates available : entrances) {
				for (Coordinates potential : piece.getEntrances()) {
					for (MapPieceInstance pSettings : piece.getRotationOptions(available, potential)) {
						piece.getShape().applySettings(pSettings);
						int[] offset = pSettings.calculateOffset(available);
						if (canPlace(piece.getShape(), offset[0], offset[2])) {
							int value = Math.abs(offset[0] - (MAP_SIZE / 2)) + Math.abs(offset[2] - (MAP_SIZE / 2));
							pSettings.setPotential(value);
							potentialPlacements.add(pSettings);
							pSettings.setX(offset[0]);
							pSettings.setY(offset[1]);
							pSettings.setZ(offset[2]);
						}
					}
				}
				
				// If there are already plenty of options, don't try more
				if (potentialPlacements.size() > 50) break;
			}

			if (potentialPlacements.size() == 0) return false;

			MapPieceInstance best = potentialPlacements.first();
			place(best, false);
		}
		addTargets(piece.getTargets());
		return true;
	}
	
	private boolean canPlace(MapShape shape, int x, int z) {
		for (int i = 0; i < shape.getLength(); i++) {
			for (int j = 0; j < shape.getHeight(); j++) {
				if (x + i > MAP_SIZE - 1 || x + i < 0 || z + j > MAP_SIZE - 1 || z + j < 0) return false;
				
				if (this.shape[x + i][z + j] && shape.get(i, j)) return false;
			}
		}
		return true;
	}
	
	private void place(MapPieceInstance inst, boolean deserializing) {
		MapShape shape = inst.getPiece().getShape();
		/*
		 * Unsure what this piece of code does, it doesn't actually remove anything
		 * from the entrances list, so removing for now until something breaks
		if (!deserializing && inst.getAvailableEntrance() != null) {
			entrances.remove(inst.getAvailableEntrance());
		}
		*/
		shape.applySettings(inst);
		for (int i = 0; i < shape.getLength(); i++) {
			for (int j = 0; j < shape.getHeight(); j++) {
				boolean b = shape.get(i, j);
				
				// Only do things if we're placing a tangible piece
				if (b) {
					this.shape[inst.getX() + i][inst.getZ() + j] = shape.get(i, j);

					// Remove any entrances at this location, may need to save them to properly block them
					if (!deserializing) {
						Iterator<Coordinates> iter = entrances.iterator();
						while (iter.hasNext()) {
							Coordinates entrance = iter.next();
							// TODO: May need to have an additional check for if the entrance is the one we're connecting to
							// or not. If it is, it's not a blocked entrance.
							// As it is right now, all entrances facing this shape are not filled with black concrete, even the ones
							// that aren't actually used.
							if (entrance.getXFacing() == inst.getX() + i && entrance.getZFacing() == inst.getZ() + j) {
								blockedEntrances.add(entrance);
								iter.remove();
							}
						}
					}
				}
			}
		}
		
		if (!deserializing) {
			for (Coordinates entrance : inst.getPiece().getEntrances()) {
				Coordinates coords = entrance.clone().applySettings(inst);
				if (coords.getXFacing() > MAP_SIZE - 1 || coords.getXFacing() < 0 || coords.getZFacing() > MAP_SIZE - 1 || coords.getZFacing() < 0)
					continue;
				
				 // Don't add entrance if it's already blocked
				if (this.shape[coords.getXFacing()][coords.getZFacing()]) {
					blockedEntrances.add(entrance);
					continue;
				}
				entrances.add(coords);
			}
		}
		System.out.println("Entrances final is " + entrances);

		// Set up the mobs
		for (MapSpawner spawner : inst.getPiece().getSpawners()) {
			mobs.put(spawner.getMob(), MobModifier.generateModifiers(0));
		}

		pieces.add(inst);
	}
	
	// Used to calculate blocked entrances because of how deserialization ignores entrances
	private void recalculateEntrances() {
		for (MapPieceInstance inst : pieces) {
			// First add all entrances
			for (Coordinates c : inst.getPiece().getEntrances()) {
				entrances.add(c.clone().applySettings(inst));
			}
		}
		
		for (MapPieceInstance inst : pieces) {
			for (Coordinates ent : inst.getPiece().getEntrances()) {
				Coordinates toRemove = null;
				for (Coordinates otherEnt : entrances) {
					if (ent.isFacing(otherEnt)) {
						toRemove = otherEnt;
						break;
					}
				}
				entrances.remove(toRemove);
				entrances.remove(ent);
			}
		}
	}
	
	public void cleanup() {
		
	}
	
	public void instantiate(FightInstance fi, int xOff, int zOff) {
		// First clear the board
		try (EditSession editSession = WorldEdit.getInstance().newEditSession(Area.world)) {
		    CuboidRegion r = new CuboidRegion(
		    		BlockVector3.at(-(xOff + MapPieceInstance.X_FIGHT_OFFSET), 0, MapPieceInstance.Z_FIGHT_OFFSET + zOff),
		    		BlockVector3.at(-(xOff + MapPieceInstance.X_FIGHT_OFFSET + MAP_SIZE * 16), 128, MapPieceInstance.Z_FIGHT_OFFSET + zOff + MAP_SIZE * 16));
		    Mask mask = new ExistingBlockMask(editSession);
		    try {
			    editSession.replaceBlocks(r, mask, BukkitAdapter.adapt(Material.AIR.createBlockData()));
			} catch (WorldEditException e) {
				e.printStackTrace();
			}
			
		    new BukkitRunnable() {
		    	public void run() {
					// Setup pieces and spawners
					for (MapPieceInstance inst : pieces) {
						inst.instantiate(fi, xOff, zOff);
					}
					
					// Block off all unused entrances
					World w = Bukkit.getWorld(Area.WORLD_NAME);
					for (Coordinates coords : entrances) {
						int x = -(xOff + MapPieceInstance.X_FIGHT_OFFSET + (coords.getX() * 16));
						int y = MapPieceInstance.Y_OFFSET + coords.getY();
						int z = MapPieceInstance.Z_FIGHT_OFFSET + zOff + (coords.getZ() * 16);
						int xp = x, zp = z;
						
						// Testing only
					    Location test = new Location(Bukkit.getWorld("Dev"), x, y, z);
					    test.getBlock().setType(Material.GOLD_BLOCK);
					    
					    // Remember that entrance directions are inverted due to how they're constructed
						switch (coords.getDirection()) {
						case NORTH:
							x -= 5;
							z += 15;
							xp = x - 4;
							zp = z;
							break;
						case SOUTH:
							x -= 5;
							xp = x - 4;
							z -= 1;
							zp = z;
							break;
						case EAST:
							z += 5;
							x -= 15;
							zp = z + 4;
							xp = x;
							break;
						case WEST:
							z += 5;
							zp = z + 4;
							break;
						}
						
						System.out.println(coords.getDirection().invert() + " " + x + ":" + xp + " " + y + ":" + (y+4) + " " + z + ":" + zp);
						for (int i = x; i >= xp; i--) {
							for (int j = y; j < y + 4; j++) {
								for (int k = z; k <= zp; k++) {
									Block b = new Location(w, i, j, k).getBlock();
									if (!b.getType().isOccluding()) b.setType(Material.BLACK_CONCRETE);
								}
							}
						}
						//r = new CuboidRegion(BlockVector3.at(x, y, z), BlockVector3.at(xp, y + 4, z + zp));
					    /*try {
						    editSession.replaceBlocks(r, mask, BukkitAdapter.adapt(Material.BLACK_CONCRETE.createBlockData()));
						} catch (WorldEditException e) {
							e.printStackTrace();
						}*/
					}
		    	}
		    }.runTaskLater(NeoRogue.inst(), 10L);
		}
	}
	
	private static void shufflePieces(AreaType type) {
		Collections.shuffle(usedPieces.get(type));
		standardPieces.get(type).addAll(usedPieces.get(type));
	}
	
	public static LinkedList<MapPiece> getPieces(AreaType type) {
		return standardPieces.get(type);
	}
	
	public TreeMap<Mob, ArrayList<MobModifier>> getMobs() {
		return mobs;
	}
	
	public ArrayList<MapPieceInstance> getPieces() {
		return pieces;
	}
	
	public void display() {
		System.out.println("X left/right, Z up/down");
		for (int z = shape[0].length - 1; z >= 0; z--) {
			System.out.print(z + ": ");
			for (int x = 0; x < shape.length; x++) {
				System.out.print(shape[x][z] ? "X" : ".");
			}
			System.out.println();
		}
	}
	
	public void addTargets(Collection<String> targets) {
		if (targets == null) return;
		this.targets.addAll(targets);
	}
	
	public HashSet<String> getTargets() {
		return targets;
	}
	
	public String serialize() {
		String str = "";
		for (MapPieceInstance mpi : pieces) {
			str += mpi.serialize() + ";";
		}
		return str;
	}
	
	public static Map deserialize(String str) {
		Map map = new Map();
		String[] pieces = str.split(";");
		for (String piece : pieces) {
			map.place(MapPieceInstance.deserialize(piece), true);
		}
		map.recalculateEntrances();
		return map;
	}
}
