package me.neoblade298.neorogue.map;

import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.shared.io.Section;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.region.Region;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.Mob;
import me.neoblade298.neorogue.session.fight.MobModifier;

public class Map {
	private static HashMap<String, MapPiece> allPieces = new HashMap<String, MapPiece>();
	protected static HashMap<RegionType, LinkedList<MapPiece>> standardPieces = new HashMap<RegionType, LinkedList<MapPiece>>(),
			usedPieces = new HashMap<RegionType, LinkedList<MapPiece>>();
	private static HashMap<RegionType, ArrayList<MapPiece>> minibossPieces = new HashMap<RegionType, ArrayList<MapPiece>>(),
			bossPieces = new HashMap<RegionType, ArrayList<MapPiece>>();
	protected static final int MAP_SIZE = 12;
	
	private RegionType type;
	private boolean hasCustomMobInfo = false;
	private ArrayList<MapPieceInstance> pieces = new ArrayList<MapPieceInstance>();
	protected LinkedList<MapEntrance> entrances = new LinkedList<MapEntrance>(),
			obstructedEntrances = new LinkedList<MapEntrance>();
	private ArrayList<Coordinates> spawns = new ArrayList<Coordinates>();
	private TreeMap<Mob, ArrayList<MobModifier>> mobs = new TreeMap<Mob, ArrayList<MobModifier>>();
	private LinkedHashMap<Mob, ArrayList<MobModifier>> customMobs = new LinkedHashMap<Mob, ArrayList<MobModifier>>();
	private HashSet<String> targets = new HashSet<String>();
	private boolean[][] shape = new boolean[MAP_SIZE][MAP_SIZE];
	private int effectiveSize;
	private ArrayList<MapEntrance[]> connectedEntrances = new ArrayList<>();
	
	public Map(RegionType type) {
		this.type = type;
	}
	
	/**
	 * Factory method to create the correct Map subclass for a given RegionType.
	 */
	protected static Map createMap(RegionType type) {
		switch (type) {
		case HARVEST_FIELDS:
		case HARVEST_FIELDS_DEBUG:
			return new HarvestFieldsMap(type);
		case FROZEN_WASTES:
		case FROZEN_WASTES_DEBUG:
			return new FrozenWastesMap(type);
		default:
			return new Map(type);
		}
	}
	
	public static void load() {
		for (RegionType type : RegionType.values()) {
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
					RegionType region = RegionType.valueOf(sec.getString("region"));
					String type = sec.getString("type", "STANDARD");

					allPieces.put(key, piece);
					if (type.equals("BOSS")) bossPieces.get(region).add(piece);
					else if (type.equals("MINIBOSS")) minibossPieces.get(region).add(piece);
					else if (type.equals("STANDARD")) standardPieces.get(region).add(piece);
					// Do not turn into an else, there are other types like BORDER pieces
				}
				catch (Exception e) {
					e.printStackTrace();
					Bukkit.getLogger().warning("[NeoRogue] Failed to load MapPiece " + key + " in file " + file.getName());
				}
			}
		});
		
		for (RegionType type : RegionType.values()) {
			Collections.shuffle(standardPieces.get(type));
		}
	}
	
	public static void reloadMythicMobs() {
		for (MapPiece piece : allPieces.values()) {
			piece.reloadMythicMobs();
		}
	}
	
	public static Map generateBoss(RegionType type, int numPieces, boolean debugMode) {
        ArrayList<MapPiece> pieces = bossPieces.get(debugMode ? RegionType.getDebugRegion(type) : type);
		MapPiece piece = pieces.get(NeoRogue.gen.nextInt(pieces.size()));
		return generate(type, numPieces, piece, debugMode);
	}
	
	public static Map generateMiniboss(RegionType type, int numPieces, boolean debugMode, String exclude) {
        ArrayList<MapPiece> pieces = minibossPieces.get(debugMode ? RegionType.getDebugRegion(type) : type);
		ArrayList<MapPiece> candidates = pieces;
		if (exclude != null && pieces.size() > 1) {
			candidates = new ArrayList<MapPiece>();
			for (MapPiece mp : pieces) {
				if (!mp.getId().equals(exclude)) {
					candidates.add(mp);
				}
			}
		}
		MapPiece piece = candidates.get(NeoRogue.gen.nextInt(candidates.size()));
		return generate(type, numPieces, piece, debugMode);
	}
	
	public static Map generate(RegionType type, int numPieces, boolean debugMode) {
		Map map = createMap(type);
		if (map instanceof FrozenWastesMap) {
			FrozenWastesMap fwMap = (FrozenWastesMap) map;
			fwMap.generatePieces(type, debugMode);
			return fwMap;
		}
		return generate(map, type, numPieces, debugMode);
	}
	
	public static Map generate(RegionType type, int numPieces, MapPiece requiredPiece, boolean debugMode) {
		Map map = new Map(type);
		map.place(requiredPiece);
		return generate(map, type, numPieces, debugMode);
	}
	
	public static Map generate(Map map, RegionType type, int numPieces, boolean debugMode) {
		LinkedList<MapPiece> genAvailPieces = standardPieces.get(debugMode ? RegionType.getDebugRegion(type) : type);
        LinkedList<MapPiece> genUsedPieces = usedPieces.get(debugMode ? RegionType.getDebugRegion(type) : type);
        
		int totalAttempts = genAvailPieces.size() + genUsedPieces.size();
		
		for (int i = 0; i < numPieces; i++) {
			MapPiece piece = null;
			int attempts = 0;
			do {
				if (genAvailPieces.size() == 0) {
                    Collections.shuffle(genUsedPieces);
                    genAvailPieces.addAll(genUsedPieces);
                }
                
				piece = genAvailPieces.poll();
				genUsedPieces.add(piece);
				
				if (attempts++ > totalAttempts) {
					Bukkit.getLogger().warning("[NeoRogue] Ran out of valid pieces to place. Returning map as is.");
					break;
				}
			}
			/* Skip to another piece if
			* 1: we have 1 entrance left and the next piece is 1 entrance and there are still more pieces we need to place
			* 2: the piece cannot be placed anywhere on the map
			*/
			while ((map.entrances.size() < 2 && piece.getNumEntrances() < 2 && i < numPieces - 1) || !map.place(piece));

			if (piece == null) {
				Bukkit.getLogger().warning("[NeoRogue] Failed to find piece for generation. Returning map as is.");
				break;
			}
		}
		
		map.postGenerate(type);
		
		return map;
	}
	
	public void placeFirst(MapPiece piece, boolean randomizeOrientation) {
		MapPieceInstance inst = piece.getInstance();
		MapShape shape = inst.getPiece().getShape();
		
		if (randomizeOrientation) {
			// Randomly rotate the piece
			inst.setRotations(NeoRogue.gen.nextInt(4));
			int rand = NeoRogue.gen.nextInt(3);
			if (rand == 1) inst.setFlip(true, false);
			else if (rand == 2) inst.setFlip(false, true);
			shape.applySettings(inst);
		}
		
		// Place the piece in the middle of the board
		int x = (MAP_SIZE / 2) - (shape.getHeight() / 2);
		int z = (MAP_SIZE / 2) - (shape.getLength() / 2);
		
		inst.setX(x);
		inst.setY(0);
		inst.setZ(z);
		placePiece(inst, false);
	}
	
	public boolean place(MapPiece piece) {
		addTargets(piece.getTargets());
		if (pieces.size() == 0) {
			// Special case, first piece being placed
			placeFirst(piece, true);
		}
		// Standard case, find an existing entrance and try to put the piece on
		else {
			TreeSet<MapPieceInstance> potentialPlacements = new TreeSet<MapPieceInstance>();
			for (MapEntrance available : entrances) {
				for (MapEntrance potential : piece.getEntrances()) {
					if (!available.tagsCompatible(potential)) continue;
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
			placePiece(best, false);
		}
		return true;
	}
	
	protected boolean canPlace(MapShape shape, int x, int z) {
		for (int i = 0; i < shape.getLength(); i++) {
			for (int j = 0; j < shape.getHeight(); j++) {
				if (x + i > MAP_SIZE - 1 || x + i < 0 || z + j > MAP_SIZE - 1 || z + j < 0) return false;
				
				if (this.shape[x + i][z + j] && shape.get(i, j)) return false;

				if (!canPlaceOnEdge() && shape.get(i, j) &&
						(x + i == 0 || x + i == MAP_SIZE - 1 || z + j == 0 || z + j == MAP_SIZE - 1)) return false;
			}
		}
		return true;
	}
	
	protected void placePiece(MapPieceInstance inst, boolean deserializing) {
		MapShape shape = inst.getPiece().getShape();
		shape.applySettings(inst);
		ArrayList<MapEntrance> obstructed = new ArrayList<MapEntrance>();
		for (int i = 0; i < shape.getLength(); i++) {
			for (int j = 0; j < shape.getHeight(); j++) {
				// Place 1 chunk of the shape, skip if it's not a used chunk
				if (!shape.get(i, j)) continue;
				
				this.shape[inst.getX() + i][inst.getZ() + j] = shape.get(i, j);

				// Make note of obstructed entrances on the map
				if (!deserializing) {
					Iterator<MapEntrance> iter = entrances.iterator();
					while (iter.hasNext()) {
						MapEntrance entrance = iter.next();
						if (entrance.getXFacing() == inst.getX() + i && entrance.getZFacing() == inst.getZ() + j) {
							obstructed.add(entrance);
							iter.remove();
						}
					}
				}
			}
		}
		
		if (!deserializing) {
			if (inst.getPiece().getEntrances() != null) {
				for (MapEntrance entrance : inst.getPiece().getEntrances()) {
					MapEntrance coords = entrance.clone().applySettings(inst);
					
					// Don't add entrance if it's connecting with another entrance
					Iterator<MapEntrance> iter = obstructed.iterator();
					boolean isAvailable = true;
					while (iter.hasNext()) {
						MapEntrance other = iter.next();
						if (coords.canConnect(other)) {
							connectedEntrances.add(new MapEntrance[] { other, coords });
							isAvailable = false;
							iter.remove();
						}
					}
					if (!isAvailable) continue;
					
					// An entrance that cannot be used because another piece is in the way but doesn't have a corresponding entrance
					if ((int) coords.getXFacing() < 0 || (int) coords.getZFacing() < 0 ||
							this.shape.length <= (int) coords.getXFacing() || this.shape[0].length <= (int) coords.getZFacing() || 
							this.shape[(int) coords.getXFacing()][(int) coords.getZFacing()]) {
						obstructedEntrances.add(coords);
					}
					// An available entrance to use
					else {
						entrances.add(coords);
					}
				}
			}
			
			for (MapEntrance ent : obstructed) {
				obstructedEntrances.add(ent);
			}
		}

		// Set up the mobs
		if (inst.getPiece().hasCustomMobInfo()) {
			hasCustomMobInfo = true;
			for (String str : inst.getPiece().getCustomMobInfo()) {
				Mob mob = Mob.get(str);
				if (mob == null) {
					Bukkit.getLogger().warning("[NeoRogue] Failed to load mob " + str + " from custom mob info in mappiece " + inst.getPiece().getId());
					continue;
				}
				customMobs.put(mob, MobModifier.generateModifiers(0));
			}
		}
		
		if (customMobs.isEmpty()) {
			if (inst.getPiece().getInitialSpawns() != null) {
				for (MapSpawner spawner : inst.getPiece().getInitialSpawns()) {
					if (spawner.getMob() == null) {
						Bukkit.getLogger().warning("[NeoRogue] Failed to load map piece " + inst.getPiece().getId() + ", initial spawner had null mob " + spawner.getMobId());
						continue;
					}
					mobs.put(spawner.getMob(), MobModifier.generateModifiers(0));
				}
			}
			if (inst.getPiece().hasSpawners()) {
				for (MapSpawner spawner : inst.getPiece().getSpawners(inst.getSpawnerSet())) {
					if (spawner.getMob() == null) {
						Bukkit.getLogger().warning("[NeoRogue] Failed to load map piece " + inst.getPiece().getId() + ", spawner had null mob " + spawner.getMobId());
						continue;
					}
					mobs.put(spawner.getMob(), MobModifier.generateModifiers(0));
				}
			}
		}
		
		// Add spawns
		if (inst.getSpawns() != null) {
			for (Coordinates coords : inst.getSpawns()) {
				Coordinates applied = coords.clone().applySettings(inst);
				Bukkit.getLogger().info("[SpawnDebug/placePiece] piece=" + inst.getPiece().getId()
						+ " pieceY=" + inst.getY() + " rawY=" + coords.getY()
						+ " appliedGetY=" + applied.getY()
						+ " toLocY=" + applied.toLocation().getY());
				spawns.add(applied);
			}
		}
		if (!inst.getPiece().ignoreSize()) effectiveSize += inst.getPiece().getShape().getChunkCount();
		pieces.add(inst);
	}
	
	// Used to calculate blocked entrances because of how deserialization ignores entrances
	private void recalculateEntrances() {
		for (MapPieceInstance inst : pieces) {
			// First add all entrances
			if (inst.getPiece().getEntrances() != null) {
				for (MapEntrance c : inst.getPiece().getEntrances()) {
					entrances.add(c.clone().applySettings(inst));
				}
			}
		}
		
		// Remove connected entrances and record pairs
		ArrayList<MapEntrance> toRemove = new ArrayList<MapEntrance>();
		HashSet<MapEntrance> alreadyPaired = new HashSet<>();
		for (MapEntrance ent1 : entrances) {
			if (alreadyPaired.contains(ent1)) continue;
			for (MapEntrance ent2 : entrances) {
				if (ent1 == ent2 || alreadyPaired.contains(ent2)) continue;
				if (ent1.canConnect(ent2)) {
					connectedEntrances.add(new MapEntrance[] { ent1, ent2 });
					alreadyPaired.add(ent1);
					alreadyPaired.add(ent2);
					toRemove.add(ent1);
					toRemove.add(ent2);
					break;
				}
			}
		}

		for (MapEntrance rem : toRemove) {
			entrances.remove(rem);
		}
	}

	protected void postDeserialize() {
	}

	// Used to calculate rough chunk size of the map
	public int getEffectiveSize() {
		return effectiveSize;
	}

	public boolean hasCustomMobInfo() {
		return hasCustomMobInfo;
	}
	
	public void cleanup() {
		
	}
	
	public void instantiate(FightInstance fi, int xOff, int zOff) {
		// Compute bounding box of occupied chunks with padding for terrain
		int minGX = MAP_SIZE, maxGX = 0, minGZ = MAP_SIZE, maxGZ = 0;
		for (int gx = 0; gx < MAP_SIZE; gx++) {
			for (int gz = 0; gz < MAP_SIZE; gz++) {
				if (!shape[gx][gz]) continue;
				minGX = Math.min(minGX, gx);
				maxGX = Math.max(maxGX, gx);
				minGZ = Math.min(minGZ, gz);
				maxGZ = Math.max(maxGZ, gz);
			}
		}
		// Padding of 3 chunks accounts for terrain generation radius (PADDING_CHUNKS=2 + 1 extra)
		int padding = 3;
		minGX = Math.max(0, minGX - padding);
		maxGX = Math.min(MAP_SIZE - 1, maxGX + padding);
		minGZ = Math.max(0, minGZ - padding);
		maxGZ = Math.min(MAP_SIZE - 1, maxGZ + padding);
		int clearStartX = minGX * 16;
		int clearEndX = (maxGX + 1) * 16;
		int clearStartZ = minGZ * 16;
		int clearEndZ = (maxGZ + 1) * 16;

		// First clear the board (only the used region)
		// Clear Y range: pieces at Y_OFFSET (64) with offsets -4 to +4, mountains up to +35, barriers above that
		int clearMinY = MapPieceInstance.Y_OFFSET - 10;
		int clearMaxY = 120;

		long clearStart = System.currentTimeMillis();
		EditSession editSession = WorldEdit.getInstance().newEditSession(Region.world);
		CuboidRegion r = new CuboidRegion(
				BlockVector3.at(-(xOff + MapPieceInstance.X_FIGHT_OFFSET + clearStartX), clearMinY, MapPieceInstance.Z_FIGHT_OFFSET + zOff + clearStartZ),
				BlockVector3.at(-(xOff + MapPieceInstance.X_FIGHT_OFFSET + clearEndX), clearMaxY, MapPieceInstance.Z_FIGHT_OFFSET + zOff + clearEndZ));
		try {
			editSession.replaceBlocks(r, new ExistingBlockMask(editSession), BukkitAdapter.adapt(Material.AIR.createBlockData()));
		} catch (WorldEditException e) {
			e.printStackTrace();
		}
		long replaceTime = System.currentTimeMillis() - clearStart;
		long flushStart = System.currentTimeMillis();
		editSession.close();
		long flushTime = System.currentTimeMillis() - flushStart;
		Bukkit.getLogger().info("[Map] WorldEdit clear: replace=" + replaceTime + "ms, flush=" + flushTime + "ms (" + (clearEndX - clearStartX) + "x" + (clearEndZ - clearStartZ) + " area, Y " + clearMinY + "-" + clearMaxY + ")");

		long postClearStart = System.currentTimeMillis();
		generateTerrain(fi, xOff, zOff);
		long terrainTime = System.currentTimeMillis() - postClearStart;

		// Setup pieces and spawners
		if (fi != null) {
			for (MapPieceInstance inst : pieces) {
				// Setup mythic locations first before spawning anything
				for (Entry<String, Coordinates> ent : inst.getMythicLocations().entrySet()) {
					Location loc = ent.getValue().applySettings(inst).toLocation();
					loc.add(xOff + MapPieceInstance.X_FIGHT_OFFSET,
							MapPieceInstance.Y_OFFSET,
							MapPieceInstance.Z_FIGHT_OFFSET + zOff);
					loc.setX(-loc.getX() + (loc.getX() % 1 != 0 ? 1 : 0));
					fi.addMythicLocation(ent.getKey(), loc);
				}
			}
		}

		long pieceStart = System.currentTimeMillis();
		for (MapPieceInstance inst : pieces) {
			inst.instantiate(fi, xOff, zOff);
		}
		long pieceTime = System.currentTimeMillis() - pieceStart;

		long entranceStart = System.currentTimeMillis();
		// Block off all unused entrances
		if (shouldBlockEntrances()) {
			World w = Bukkit.getWorld(Region.getActiveWorldName());
			for (MapEntrance coords : entrances) {
				handleUnusedEntrance(coords, w, xOff, zOff);
			}
			for (MapEntrance coords : obstructedEntrances) {
				handleObstructedEntrance(coords, w, xOff, zOff);
			}
		}
		long entranceTime = System.currentTimeMillis() - entranceStart;
		long total = System.currentTimeMillis() - clearStart;
		Bukkit.getLogger().info("[Map] Instantiate: terrain=" + terrainTime + "ms, pieces=" + pieceTime + "ms, entrances=" + entranceTime + "ms, total=" + total + "ms");
	}
	
	/**
	 * Override in subclasses to generate custom terrain (mountains, paths, etc.).
	 * Called during instantiate before pieces are pasted.
	 * Base implementation generates entrance-based mountain paths for spaced maps.
	 */
	protected void generateTerrain(FightInstance fi, int xOff, int zOff) {
	}
	
	/**
	 * Whether unused/obstructed entrances should be blocked off with walls.
	 * Override to return false for maps where terrain handles containment.
	 */
	protected boolean shouldBlockEntrances() {
		return true;
	}
	
	/**
	 * Whether pieces can be placed on the edge of the map grid.
	 * Override to return false for maps that reserve edges (e.g. for border pieces).
	 */
	protected boolean canPlaceOnEdge() {
		return true;
	}
	
	/**
	 * Called after the standard piece placement loop in generate().
	 * Override to add border pieces or other post-generation modifications.
	 */
	protected void postGenerate(RegionType type) {
	}
	
	private void handleUnusedEntrance(MapEntrance coords, World w, int xOff, int zOff) {
		handleObstructedEntrance(coords, w, xOff, zOff);
	}
	
	private void handleObstructedEntrance(MapEntrance coords, World w, int xOff, int zOff) {
		int x = (int) -(xOff + MapPieceInstance.X_FIGHT_OFFSET + (coords.getX() * 16));
		int y = (int) (MapPieceInstance.Y_OFFSET + coords.getY());
		int z = (int) (MapPieceInstance.Z_FIGHT_OFFSET + zOff + (coords.getZ() * 16));
		int xp = x, zp = z;
	    
	    // Remember that entrance directions are inverted due to how they're constructed
		switch (coords.getDirection()) {
		case NORTH:
			x -= 5;
			z += 16;
			xp = x - 5;
			zp = z;
			break;
		case SOUTH:
			x -= 5;
			xp = x - 5;
			z -= 1;
			zp = z;
			break;
		case EAST:
			z += 5;
			x -= 16;
			zp = z + 5;
			xp = x;
			break;
		case WEST:
			x += 1;
			xp += 1;
			z += 5;
			zp = z + 5;
			break;
		}

		for (int i = x; i >= xp; i--) {
			for (int j = y - 1; j < y + 5; j++) {
				for (int k = z; k <= zp; k++) {
					Block b = new Location(w, i, j, k).getBlock();
					b.setType(Material.BLACK_CONCRETE);
				}
			}
		}
	}
	
	public static HashMap<String, MapPiece> getAllPieces() {
		return allPieces;
	}
	
	public static HashMap<RegionType, ArrayList<MapPiece>> getMinibossPieces() {
		return minibossPieces;
	}
	
	public static HashMap<RegionType, ArrayList<MapPiece>> getBossPieces() {
		return bossPieces;
	}
	
	public static LinkedList<MapPiece> getPieces(RegionType type) {
		return standardPieces.get(type);
	}

	public static boolean hasPiecesForRegion(RegionType type) {
		LinkedList<MapPiece> standard = standardPieces.get(type);
		LinkedList<MapPiece> used = usedPieces.get(type);
		return (standard != null && !standard.isEmpty()) || (used != null && !used.isEmpty());
	}
	
	public AbstractMap<Mob, ArrayList<MobModifier>> getMobs() {
		return customMobs.isEmpty() ? mobs : customMobs;
	}
	
	public ArrayList<MapPieceInstance> getPieces() {
		return pieces;
	}
	
	public void display() {
		System.out.println("X left/right, Z up/down");
		for (int z = shape[0].length - 1; z >= 0; z--) {
			char zc = Character.forDigit(z, 10);
			if (z == 10) zc = 'A';
			if (z == 11) zc = 'B';
			System.out.print(zc + ": ");
			
			
			for (int x = 0; x < shape.length; x++) {
				char symbol = '.';
				for (MapEntrance entrance : entrances) {
					if (entrance.getXFacing() == x && entrance.getZFacing() == z) {
						symbol = 'E';
					}
				}
				if (shape[x][z]) symbol = 'X';
				System.out.print(symbol);
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
		String str = type + "-";
		for (MapPieceInstance mpi : pieces) {
			str += mpi.serialize() + ";";
		}
		return str;
	}
	
	public Coordinates getRandomSpawn() {
		int rand = NeoRogue.gen.nextInt(spawns.size());
		return spawns.get(rand);
	}
	
	protected static LinkedList<MapPiece> getStandardPieces(RegionType type) {
		return standardPieces.get(type);
	}
	
	protected static LinkedList<MapPiece> getUsedPieces(RegionType type) {
		return usedPieces.get(type);
	}
	
	public List<MapEntrance[]> getConnectedEntrances() {
		return connectedEntrances;
	}
	
	public boolean[][] getShape() {
		return shape;
	}
	
	public static int getMapSize() {
		return MAP_SIZE;
	}
	
	public static Map deserialize(String str) {
		RegionType type = RegionType.valueOf(str.substring(0, str.indexOf("-")));
		Map map = createMap(type);
		str = str.substring(str.indexOf("-") + 1);
		String[] pieces = str.split(";");
		for (String piece : pieces) {
			MapPieceInstance mpi = MapPieceInstance.deserialize(piece);
			map.placePiece(mpi, true);
			map.addTargets(mpi.getPiece().getTargets());
		}
		map.recalculateEntrances();
		map.postDeserialize();
		return map;
	}
}


