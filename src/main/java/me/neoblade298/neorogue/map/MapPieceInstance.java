package me.neoblade298.neorogue.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.mask.SolidBlockMask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.region.Region;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.fight.FightInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class MapPieceInstance implements Comparable<MapPieceInstance> {
	public static final int Z_FIGHT_OFFSET = 0, Y_OFFSET = 64, X_FIGHT_OFFSET = 49;
	
	private MapPiece piece;
	private Coordinates[] spawns;
	private HashMap<String, Coordinates> mythicLocations = new HashMap<String, Coordinates>();
	private MapEntrance entrance, available;
	protected int numRotations;
	private int x, y, z; // In chunk offset
	protected boolean flipX, flipZ;
	private ClipboardHolder schematic;
	private int potential = 100;
	private int len, hgt;
	private int spawnerIdx;
	private int[] rotateOffset = new int[] {0, 0},
			flipOffset = new int[] {0, 0};
	
	protected MapPieceInstance(MapPiece piece) {
		this.piece = piece;
		schematic = new ClipboardHolder(piece.clipboard);
		spawnerIdx = piece.chooseSpawners();
		
		if (piece.spawns != null) {
			spawns = new Coordinates[piece.spawns.length];
			int i = 0;
			for (Coordinates coords : piece.spawns) {
				spawns[i++] = coords.clone();
			}
		}
		
		for (Entry<String, Coordinates> ent : piece.mythicLocations.entrySet()) {
			mythicLocations.put(ent.getKey(), ent.getValue());
		}
		
		MapShape shape = piece.getShape();
		len = shape.getBaseLength() * 16 - 1;
		hgt = shape.getBaseHeight() * 16 - 1;
	}
	
	protected MapPieceInstance(MapPiece piece, MapEntrance available, MapEntrance toAttach) {
		this(piece);
		this.available = available.clone();
		this.entrance = toAttach.clone().applySettings(this);
	}
	
	public static MapPieceInstance deserialize(String str) {
		String[] split = str.split(",");
		MapPieceInstance mpi = new MapPieceInstance(MapPiece.get(split[0]));
		mpi.setX(Integer.parseInt(split[1]));
		mpi.setY(Integer.parseInt(split[2]));
		mpi.setZ(Integer.parseInt(split[3]));
		mpi.setRotations(Integer.parseInt(split[4]));
		mpi.setFlip(split[5].equals("T"), split[6].equals("T"));
		mpi.spawnerIdx = Integer.parseInt(split[7]);
		return mpi;
	}
	
	public String serialize() {
		return piece.getId() + "," + x + "," + y + "," + z + "," + numRotations + "," + (flipX ? "T" : "F") + "," + (flipZ ? "T" : "F") + "," + spawnerIdx;
	}
	
	public int getNumRotations() {
		return numRotations;
	}
	public boolean isFlipX() {
		return flipX;
	}
	public boolean isFlipZ() {
		return flipZ;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (flipX ? 1231 : 1237);
		result = prime * result + (flipZ ? 1231 : 1237);
		result = prime * result + numRotations;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		MapPieceInstance other = (MapPieceInstance) obj;
		if (flipX != other.flipX) return false;
		if (flipZ != other.flipZ) return false;
		if (numRotations != other.numRotations) return false;
		return true;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getZ() {
		return z;
	}

	public String getType() {
		return piece.getType();
	}
	
	public void setRotations(int amount) {
		numRotations = amount % 4;
		if (entrance != null) entrance.setRotations(numRotations);
		
		if (spawns != null) {
			for (Coordinates coords : spawns) {
				coords.setRotations(amount);
			}
		}
		
		for (Coordinates coords : mythicLocations.values()) {
			coords.setRotations(amount);
		}
	}
	
	public void setFlip(boolean flipX, boolean flipZ) {
		if (flipX && flipZ) {
			this.flipX = false;
			this.flipZ = false;
			setRotations((numRotations + 2) % 4); // A double flip is just a 180 rotation
			return;
		}
		
		this.flipX = flipX;
		this.flipZ = flipZ;
		
		if (entrance != null) {
			entrance.setFlip(flipX, flipZ);
		}
		
		if (spawns != null) {
			for (Coordinates coords : spawns) {
				coords.setFlip(flipX, flipZ);
			}
		}
		
		for (Coordinates coords : mythicLocations.values()) {
			coords.setFlip(flipX, flipZ);
		}
	}
	
	private void updateSchematic() {
		switch (numRotations) {
		case 0: rotateOffset[0] = 0;
		rotateOffset[1] = 0;
		break;
		case 1: rotateOffset[0] = 0;
		rotateOffset[1] = len;
		break;
		case 2: rotateOffset[0] = len;
		rotateOffset[1] = hgt;
		break;
		case 3: rotateOffset[0] = hgt;
		rotateOffset[1] = 0;
		break;
		}
		
		AffineTransform transform = new AffineTransform();
		// It is only possible for one of these to be true at a time
		flipOffset[0] = 0;
		flipOffset[1] = 0;
		if (flipX || flipZ) {
			if (flipX) {
				switch (numRotations) {
				case 0: flipOffset[1] = hgt;
				break;
				case 1: flipOffset[1] = -len;
				break;
				case 2: flipOffset[1] = -hgt;
				break;
				case 3: flipOffset[1] = len;
				break;
				}
			}
			else {
				switch (numRotations) {
				case 0: flipOffset[0] = len;
				break;
				case 1: flipOffset[0] = hgt;
				break;
				case 2: flipOffset[0] = -len;
				break;
				case 3: flipOffset[0] = -hgt;
				break;
				}
			}
			BlockVector3 direction = BlockVector3.at(flipZ ? 1 : 0, 0, flipX ? 1 : 0);
			transform = transform.scale(direction.abs().multiply(-2).add(1, 1, 1).toVector3());
			schematic.setTransform(transform);
		}
		transform = transform.rotateY(numRotations * -90);
		schematic.setTransform(transform);
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public void setZ(int z) {
		this.z = z;
	}
	
	public MapPiece getPiece() {
		return piece;
	}
	
	public void rotateToFace(MapEntrance existing, MapEntrance toAttach) {
		int amount = (existing.getDirection().getValue() - toAttach.getOriginalDirection().getValue() + 6) % 4;
		setRotations(amount);
	}
	
	public void flipOppositeAxis() {
		int val = entrance.getDirection().getValue();
		setFlip(val % 2 == 1, val % 2 == 0);
	}
	
	public int[] calculateOffset(MapEntrance available) {
		return new int[] { (int) (available.getXFacing() - entrance.getX()), (int) (available.getYFacing() - entrance.getY()),
				(int) (available.getZFacing() - entrance.getZ()) };
	}
	
	public int getPotential() {
		return potential;
	}
	
	public void setPotential(int potential) {
		this.potential = potential;
	}
	
	// Pastes the map piece and sets up its spawners
	public void instantiate(FightInstance fi, int xOff, int zOff) {
		updateSchematic();
		/*
		 * this.x is the chunk coordinates within the fighting area
		 * xOff is the offset of the plot
		 * Z_FIGHT_OFFSET is the offset of where the fighting area is in the plot
		 * x is negative because south is +z and right of south is -x
		 */
		int x = -((this.x * 16) + rotateOffset[0] + flipOffset[0] + xOff + X_FIGHT_OFFSET);
		int y = Y_OFFSET + this.y;
		int z = (this.z * 16) + rotateOffset[1] + flipOffset[1] + zOff + Z_FIGHT_OFFSET;
		
		try (EditSession editSession = WorldEdit.getInstance().newEditSession(Region.world)) {
		    Operation pasteSolid = schematic.createPaste(editSession)
		    		.maskSource(new SolidBlockMask(Region.world))
		            .to(BlockVector3.at(x, y, z))
		            .ignoreAirBlocks(true)
		            .build();
		    Operation pasteRemaining = schematic.createPaste(editSession)
		    		.maskSource(Masks.negate(new SolidBlockMask(Region.world)))
		            .to(BlockVector3.at(x, y, z))
		            .ignoreAirBlocks(true)
		            .build();
		    try {
				Operations.complete(pasteSolid);
				Operations.complete(pasteRemaining);
			} catch (WorldEditException e) {
				e.printStackTrace();
			}
		}
		// Instantiate spawners; if fi is null, that means it's a testmap
		if (fi != null) {
			Session s = fi.getSession();
			if (piece.getInitialSpawns() != null) {
				for (MapSpawner spawner : piece.getInitialSpawns()) {
					fi.addInitialSpawn(spawner.instantiate(s, this, xOff, zOff));
				}
			}
			if (piece.hasSpawners()) {
				for (MapSpawner spawner : piece.getSpawners(spawnerIdx)) {
					fi.addSpawner(spawner.instantiate(s, this, xOff, zOff));
				}
			}
		}
		else {
			if (piece.getInitialSpawns() != null) {
				for (MapSpawner spawner : piece.getInitialSpawns()) {
					spawner.instantiate(null, this, xOff, zOff);
				}
			}
			if (piece.hasSpawners()) {
				for (MapSpawner spawner : piece.getSpawners(spawnerIdx)) {
					spawner.instantiate(null, this, xOff, zOff);
				}
			}
		}
	}
	
	
	public MapEntrance getEntrance() {
		return entrance;
	}
	
	public MapEntrance getAvailableEntrance() {
		return available;
	}
	
	public Coordinates[] getSpawns() { 
		return spawns;
	}
	
	// Places or appends colored text to a sign at the given location, tracking line usage
	private void placeOrAppendSign(Location signLoc, Direction dir, String text, TextColor color, HashMap<Location, Integer> signLines) {
		Location key = signLoc.toBlockLocation();
		int line = signLines.getOrDefault(key, 0);
		if (line > 3) return; // Already overflowed
		if (line == 3) {
			// Sign is full, write overflow indicator
			Sign sign = (Sign) signLoc.getBlock().getState();
			sign.getSide(org.bukkit.block.sign.Side.FRONT).line(3, Component.text("...").color(NamedTextColor.GRAY));
			sign.update();
			signLines.put(key, line + 1);
			return;
		}

		if (line == 0) {
			// First time placing at this location
			Block signBlock = signLoc.getBlock();
			signBlock.setType(Material.OAK_SIGN);
			Rotatable signData = (Rotatable) signBlock.getBlockData();
			switch (dir) {
			case NORTH: signData.setRotation(BlockFace.NORTH); break;
			case SOUTH: signData.setRotation(BlockFace.SOUTH); break;
			case EAST: signData.setRotation(BlockFace.EAST); break;
			case WEST: signData.setRotation(BlockFace.WEST); break;
			}
			signBlock.setBlockData(signData);
		}

		Sign sign = (Sign) signLoc.getBlock().getState();
		sign.getSide(org.bukkit.block.sign.Side.FRONT).line(line, Component.text(text).color(color));
		sign.update();
		signLines.put(key, line + 1);
	}

	// Marks spawn locations with directional terracotta and returns the player spawn locations
	public ArrayList<Location> markSpawns(Player p, int xOff, int zOff) {
		ArrayList<Location> playerSpawnLocs = new ArrayList<>();
		HashMap<Location, Integer> signLines = new HashMap<>();

		// Mark spawners with orange wool and signs
		for (MapSpawner[] list : piece.getSpawnerSets()) {
			for (MapSpawner spawner : list) {
				Coordinates coords = spawner.getCoordinates().clone().applySettings(this);
				Location loc = coords.toLocation();
				loc.add(xOff + X_FIGHT_OFFSET, Y_OFFSET, Z_FIGHT_OFFSET + zOff);
				loc.setX(-loc.getX() + (loc.getX() % 1 != 0 ? 1 : 0));
				if (p != null) {
					if (loc.getBlock().getType().isSolid()) {
						Util.msg(p, "<red>A spawner appears to be inside a block.");
						Util.msg(p, "<red>Coords: " + Util.locToString(loc, false, false));
						Util.msg(p, "<red>Block: " + loc.getBlock().getType());
					}
					if (loc.clone().add(0, -1, 0).getBlock().getType().isAir()) {
						Util.msg(p, "<red>A spawner appears to be floating or off map.");
						Util.msg(p, "<red>Coords: " + Util.locToString(loc, false, false));
					}
				}
				loc.getBlock().setType(Material.ORANGE_WOOL);
				Location signLoc = loc.clone().add(0, 1, 0);
				if (!signLines.containsKey(signLoc.toBlockLocation())) {
					Coordinates origCoords = spawner.getCoordinates();
					String coordLabel = "MS " + (int) origCoords.getX() + ", " + (int) origCoords.getY() + ", " + (int) origCoords.getZ();
					placeOrAppendSign(signLoc, coords.getDirection(), coordLabel, NamedTextColor.GOLD, signLines);
				}
				placeOrAppendSign(signLoc, coords.getDirection(), spawner.getMobId(), NamedTextColor.GOLD, signLines);
			}
		}

		// Mark initial spawns
		if (piece.getInitialSpawns() != null) {
			for (MapSpawner initialSpawner : piece.getInitialSpawns()) {
				Coordinates coords = initialSpawner.getCoordinates().clone().applySettings(this);
				Location loc = coords.toLocation();
				loc.add(xOff + X_FIGHT_OFFSET, Y_OFFSET, Z_FIGHT_OFFSET + zOff);
				loc.setX(-loc.getX() + (loc.getX() % 1 != 0 ? 1 : 0));
				if (p != null) {
					if (loc.getBlock().getType().isSolid()) {
						Util.msg(p, "<red>An initial spawn appears to be inside a block.");
						Util.msg(p, "<red>Coords: " + Util.locToString(loc, false, false));
						Util.msg(p, "<red>Block: " + loc.getBlock().getType());
					}
					if (loc.clone().add(0, -1, 0).getBlock().getType().isAir()) {
						Util.msg(p, "<red>An initial spawn appears to be floating or off map.");
						Util.msg(p, "<red>Coords: " + Util.locToString(loc, false, false));
					}
				}
				Block b = loc.getBlock();
				b.setType(Material.MAGENTA_GLAZED_TERRACOTTA);
				Directional bmeta = (Directional) b.getBlockData();
				switch (coords.getDirection()) {
				case NORTH: bmeta.setFacing(BlockFace.SOUTH); break;
				case SOUTH: bmeta.setFacing(BlockFace.NORTH); break;
				case EAST: bmeta.setFacing(BlockFace.WEST); break;
				case WEST: bmeta.setFacing(BlockFace.EAST); break;
				}
				b.setBlockData(bmeta);

				String isLabel = "IS " + (int) coords.getX() + ", " + (int) coords.getY() + ", " + (int) coords.getZ() + ", " + coords.getDirection().flip(false).name().charAt(0);
				placeOrAppendSign(loc.clone().add(0, 1, 0), coords.getDirection(), isLabel, NamedTextColor.GREEN, signLines);
			}
		}

		// Mark player spawns
		if (spawns != null) {
			for (Coordinates c : spawns) {
				Coordinates coords = c.clone().applySettings(this);
				Location l = coords.toLocation();
				l.add(xOff + X_FIGHT_OFFSET, Y_OFFSET, Z_FIGHT_OFFSET + zOff);
				l.setX(-l.getX() + (l.getX() % 1 != 0 ? 1 : 0));
				if (p != null) {
					if (l.getBlock().getType().isSolid()) {
						Util.msg(p, "<red>A spawnpoint appears to be inside a block.");
						Util.msg(p, "<red>Coords: " + Util.locToString(l, false, false));
						Util.msg(p, "<red>Block: " + l.getBlock().getType());
					}
				}

				Block b = l.getBlock();
				b.setType(Material.MAGENTA_GLAZED_TERRACOTTA);
				Directional bmeta = (Directional) b.getBlockData();
				switch (coords.getDirection()) {
				case NORTH: bmeta.setFacing(BlockFace.SOUTH); break;
				case SOUTH: bmeta.setFacing(BlockFace.NORTH); break;
				case EAST: bmeta.setFacing(BlockFace.WEST); break;
				case WEST: bmeta.setFacing(BlockFace.EAST); break;
				}
				b.setBlockData(bmeta);

				String psLabel = "PS " + (int) coords.getX() + ", " + (int) coords.getY() + ", " + (int) coords.getZ() + ", " + coords.getDirection().flip(false).name().charAt(0);
				placeOrAppendSign(l.clone().add(0, 1, 0), coords.getDirection(), psLabel, NamedTextColor.YELLOW, signLines);
				playerSpawnLocs.add(l);
			}
		}

		// Mark mythic locations
		for (Entry<String, Coordinates> entry : mythicLocations.entrySet()) {
			Coordinates coords = entry.getValue().clone().applySettings(this);
			Location loc = coords.toLocation();
			loc.add(xOff + X_FIGHT_OFFSET, Y_OFFSET, Z_FIGHT_OFFSET + zOff);
			loc.setX(-loc.getX() + (loc.getX() % 1 != 0 ? 1 : 0));
			if (p != null) {
				if (loc.getBlock().getType().isSolid()) {
					Util.msg(p, "<red>A mythic location appears to be inside a block.");
					Util.msg(p, "<red>Coords: " + Util.locToString(loc, false, false));
					Util.msg(p, "<red>Block: " + loc.getBlock().getType());
				}
			}
			Block b = loc.getBlock();
			b.setType(Material.MAGENTA_GLAZED_TERRACOTTA);
			Directional bmeta = (Directional) b.getBlockData();
			switch (coords.getDirection()) {
			case NORTH: bmeta.setFacing(BlockFace.SOUTH); break;
			case SOUTH: bmeta.setFacing(BlockFace.NORTH); break;
			case EAST: bmeta.setFacing(BlockFace.WEST); break;
			case WEST: bmeta.setFacing(BlockFace.EAST); break;
			}
			b.setBlockData(bmeta);

			String mlLabel = "ML " + entry.getKey();
			placeOrAppendSign(loc.clone().add(0, 1, 0), coords.getDirection(), mlLabel, NamedTextColor.AQUA, signLines);
		}

		// Mark entrances
		if (piece.getEntrances() != null) {
			for (MapEntrance ent : piece.getEntrances()) {
				MapEntrance coords = ent.clone().applySettings(this);
				Coordinates entCoords = coords.getEntrance();
				// Use getX()/getZ() directly (already includes piece offset) * 16
				// Do NOT use toLocation() here — it's designed for block-level coords, not chunk-level
				double ex = entCoords.getX() * 16;
				double ez = entCoords.getZ() * 16;
				Location loc = new Location(Bukkit.getWorld(Region.getActiveWorldName()), ex, 0, ez);
				loc.add(xOff + X_FIGHT_OFFSET, Y_OFFSET + (int) entCoords.getY() - 1, Z_FIGHT_OFFSET + zOff);
				loc.setX(-loc.getX());

				// Place sign at entrance origin showing coordinates and direction (invert to match config)
				String label = "ME " + (int) entCoords.getX() + ", " + (int) entCoords.getY() + ", " + (int) entCoords.getZ() + ", " + coords.getDirection().invert().name().charAt(0);

				double entx = loc.getX();
				double entz = loc.getZ();
				switch (coords.getDirection()) {
				case NORTH:
					loc.setZ(entz + 15);
					for (double tempx = entx - 6; tempx > entx - 10; tempx--) {
						loc.setX(tempx);
						Block b = loc.getBlock();
						b.setType(b.isSolid() ? Material.RED_CONCRETE : Material.RED_STAINED_GLASS);
					}
					loc.setX(entx - 8);
					placeOrAppendSign(loc.clone().add(0, 1, 0), coords.getDirection(), label, NamedTextColor.RED, signLines);
					break;
				case SOUTH:
					for (double tempx = entx - 6; tempx > entx - 10; tempx--) {
						loc.setX(tempx);
						Block b = loc.getBlock();
						b.setType(b.isSolid() ? Material.RED_CONCRETE : Material.RED_STAINED_GLASS);
					}
					loc.setX(entx - 8);
					placeOrAppendSign(loc.clone().add(0, 1, 0), coords.getDirection(), label, NamedTextColor.RED, signLines);
					break;
				case EAST:
					loc.setX(entx - 15);
					for (double tempz = entz + 6; tempz < entz + 10; tempz++) {
						loc.setZ(tempz);
						Block b = loc.getBlock();
						b.setType(b.isSolid() ? Material.RED_CONCRETE : Material.RED_STAINED_GLASS);
					}
					loc.setZ(entz + 8);
					placeOrAppendSign(loc.clone().add(0, 1, 0), coords.getDirection(), label, NamedTextColor.RED, signLines);
					break;
				case WEST:
					for (double tempz = entz + 6; tempz < entz + 10; tempz++) {
						loc.setZ(tempz);
						Block b = loc.getBlock();
						b.setType(b.isSolid() ? Material.RED_CONCRETE : Material.RED_STAINED_GLASS);
					}
					loc.setZ(entz + 8);
					placeOrAppendSign(loc.clone().add(0, 1, 0), coords.getDirection(), label, NamedTextColor.RED, signLines);
					break;
				}
			}
		}

		return playerSpawnLocs;
	}

	public ArrayList<Location> markSpawns(int xOff, int zOff) {
		return markSpawns(null, xOff, zOff);
	}
	
	public Coordinates getMythicLocation(String key) {
		return mythicLocations.get(key);
	}
	
	public HashMap<String, Coordinates> getMythicLocations() {
		return mythicLocations;
	}
	
	public int getSpawnerSet() {
		return spawnerIdx;
	}

	@Override
	public int compareTo(MapPieceInstance o) {
		int comp = this.potential - o.potential;
		if (comp == 0) {
			return NeoRogue.gen.nextBoolean() ? 1 : -1; // Tiebreakers are random
		}
		return comp;
	}
}
