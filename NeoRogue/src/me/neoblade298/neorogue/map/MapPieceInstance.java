package me.neoblade298.neorogue.map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.session.fights.FightInstance;

public class MapPieceInstance implements Comparable<MapPieceInstance> {
	public static final int Z_FIGHT_OFFSET = 0, Y_OFFSET = 64, X_FIGHT_OFFSET = 49;
	
	private MapPiece piece;
	private Coordinates[] spawns;
	private Coordinates entrance, available;
	protected int numRotations;
	private int x, y, z; // In chunk offset
	protected boolean flipX, flipZ;
	private ClipboardHolder schematic;
	private int potential = 100;
	private int len, hgt;
	private int[] rotateOffset = new int[] {0, 0},
			flipOffset = new int[] {0, 0};
	
	protected MapPieceInstance(MapPiece piece) {
		this.piece = piece;
		schematic = new ClipboardHolder(piece.clipboard);
		
		spawns = new Coordinates[piece.spawns.length];
		int i = 0;
		for (Coordinates coords : piece.spawns) {
			spawns[i++] = coords.clone();
		}
		
		MapShape shape = piece.getShape();
		len = shape.getBaseLength() * 16 - 1;
		hgt = shape.getBaseHeight() * 16 - 1;
	}
	
	protected MapPieceInstance(MapPiece piece, Coordinates available, Coordinates toAttach) {
		this(piece);
		this.available = available.clone();
		this.entrance = toAttach.clone();
	}
	
	public static MapPieceInstance deserialize(String str) {
		String[] split = str.split(",");
		MapPieceInstance mpi = new MapPieceInstance(MapPiece.get(split[0]));
		mpi.setX(Integer.parseInt(split[1]));
		mpi.setY(Integer.parseInt(split[2]));
		mpi.setZ(Integer.parseInt(split[3]));
		mpi.setRotations(Integer.parseInt(split[4]));
		mpi.setFlip(split[5].equals("T"), split[6].equals("T"));
		return mpi;
	}
	
	public String serialize() {
		return piece.getId() + "," + x + "," + y + "," + z + "," + numRotations + "," + (flipX ? "T" : "F") + "," + (flipZ ? "T" : "F");
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
	
	public void setRotations(int amount) {
		numRotations = amount % 4;
		if (entrance != null) entrance.setRotations(numRotations);
		
		for (Coordinates coords : spawns) {
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
		for (Coordinates coords : spawns) {
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
	
	public void rotateToFace(Coordinates existing, Coordinates toAttach) {
		int amount = (existing.getDirection().getValue() - toAttach.getOriginalDirection().getValue() + 6) % 4;
		setRotations(amount);
	}
	
	public void flipOppositeAxis() {
		int val = entrance.getDirection().getValue();
		setFlip(val % 2 == 1, val % 2 == 0);
	}
	
	public int[] calculateOffset(Coordinates available) {
		entrance.applySettings(this);
		return new int[] { available.getXFacing() - entrance.getX(), available.getY() - entrance.getY(), available.getZFacing() - entrance.getZ() };
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
		int x = -((this.x * 16) + rotateOffset[0] + flipOffset[0] + xOff + X_FIGHT_OFFSET); // So that it's flush with minecraft chunks
		int y = Y_OFFSET + this.y;
		int z = (this.z * 16) + rotateOffset[1] + flipOffset[1] + zOff + Z_FIGHT_OFFSET;
		
		try (EditSession editSession = WorldEdit.getInstance().newEditSession(Area.world)) {
		    Operation operation = schematic.createPaste(editSession)
		            .to(BlockVector3.at(x, y, z))
		            .ignoreAirBlocks(false)
		            .build();
		    try {
				Operations.complete(operation);
			} catch (WorldEditException e) {
				e.printStackTrace();
			}
		}
		// Instantiate spawners; if fi is null, that means it's a testmap
		if (fi != null) {
			for (MapSpawner spawner : piece.getSpawners()) {
				fi.addSpawner(spawner.instantiate(this, xOff, zOff));
			}
		}
		else {
			for (MapSpawner spawner : piece.getSpawners()) {
				spawner.instantiate(this, xOff, zOff);
			}
		}
	}
	
	public void testPaste(World world, int xOff, int zOff) {
		updateSchematic();
		/*
		 * this.x is the chunk coordinates within the fighting area
		 * xOff is the offset of the plot
		 * X/Y/Z_FIGHT_OFFSET is the offset of where the fighting area is in the plot
		 * x is negative because south is +z and right of south is -x
		 * x/zLocal is the offset due to the player choosing the chunk to paste it in
		 */
		
		int xLocal = 1;
		int zLocal = -16;
		int x = -(rotateOffset[0] + flipOffset[0] + xOff + xLocal);
		int y = Y_OFFSET + this.y;
		int z = rotateOffset[1] + flipOffset[1] + zOff + zLocal;
		
		try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
		    Operation operation = schematic.createPaste(editSession)
		            .to(BlockVector3.at(x, y, z))
		            .ignoreAirBlocks(false)
		            .build();
		    try {
				Operations.complete(operation);
			} catch (WorldEditException e) {
				e.printStackTrace();
			}
		}
		
		// Spawners
		for (MapSpawner spawner : piece.getSpawners()) {
			Location loc = spawner.getCoordinates().clone().applySettings(this).toLocation();
			loc.setWorld(world);
			loc.add(-x - rotateOffset[0] - flipOffset[0],
					MapPieceInstance.Y_OFFSET,
					z - rotateOffset[1] - flipOffset[1]);
			loc.setX(-loc.getX());
			loc.getBlock().setType(Material.ORANGE_WOOL);
		}
		
		// Spawns
		for (Coordinates spawn : spawns) {
			Coordinates coords = spawn.clone().applySettings(this);
			Location loc = coords.toLocation();
			loc.setWorld(world);
			loc.add(-x - rotateOffset[0] - flipOffset[0],
					MapPieceInstance.Y_OFFSET,
					z - rotateOffset[1] - flipOffset[1]);
			loc.setX(-loc.getX());
			Block b = loc.getBlock();
			b.setType(Material.MAGENTA_GLAZED_TERRACOTTA);

            Directional bmeta = (Directional) b.getBlockData();
            
            // Apparently terracotta blocks point the direction opposite they're facing
            switch (coords.getDirection()) {
            case NORTH: bmeta.setFacing(BlockFace.SOUTH);
            break;
            case SOUTH: bmeta.setFacing(BlockFace.NORTH);
            break;
            case EAST: bmeta.setFacing(BlockFace.WEST);
            break;
            case WEST: bmeta.setFacing(BlockFace.EAST);
            }
            b.setBlockData(bmeta);
		}

		for (Coordinates entrance : piece.getEntrances()) {
			Coordinates coords = entrance.clone().applySettings(this);
			Location loc = coords.toLocation();
			loc.setX(loc.getX() * 16);
			loc.setZ(loc.getZ() * 16);
			loc.add(-x - rotateOffset[0] - flipOffset[0],
					MapPieceInstance.Y_OFFSET,
					z - rotateOffset[1] - flipOffset[1]);
			loc.setX(-loc.getX());
			loc.setWorld(world);
			
			double entx = loc.getX();
			double entz = loc.getZ();
			// Loc is currently on the northeast of the entrance
			
			// Iterate through the 4 entrance blocks based on entrance direction
			switch (coords.getDirection()) {
			case NORTH:
				loc.setZ(entz + 15);
				for (double tempx = entx - 6; tempx > entx - 10; tempx--) {
					loc.setX(tempx);
					loc.getBlock().setType(Material.RED_CONCRETE);
				}
				break;
			case SOUTH:
				for (double tempx = entx - 6; tempx > entx - 10; tempx--) {
					loc.setX(tempx);
					loc.getBlock().setType(Material.RED_CONCRETE);
				}
				break;
			case EAST:
				loc.setX(entx - 15);
				for (double tempz = entz + 6; tempz < entz + 10; tempz++) {
					loc.setZ(tempz);
					loc.getBlock().setType(Material.RED_CONCRETE);
				}
				break;
			case WEST:
				for (double tempz = entz + 6; tempz < entz + 10; tempz++) {
					loc.setZ(tempz);
					loc.getBlock().setType(Material.RED_CONCRETE);
				}
				break;
			}
		}
	}
	
	
	public Coordinates getEntrance() {
		return entrance;
	}
	
	public Coordinates getAvailableEntrance() {
		return available;
	}
	
	public Coordinates[] getSpawns() { 
		return spawns;
	}

	@Override
	public int compareTo(MapPieceInstance o) {
		int comp = this.potential - o.potential;
		if (comp == 0) {
			return NeoCore.gen.nextBoolean() ? 1 : -1; // Tiebreakers are random
		}
		return comp;
	}
}
