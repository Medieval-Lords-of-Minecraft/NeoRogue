package me.neoblade298.neorogue.map;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import me.neoblade298.neorogue.area.Area;

/* Assumed to always be rotating around 0,0 origin
 * After rotating, the coordinates translate themselves to be above 0,0
 * Used for MapEntrances and MapSpawners, MapEntrance uses direction too
 */
public class Coordinates extends Rotatable {
	private int x, y, z, xp, zp, xlen, zlen;
	private int xOff, yOff, zOff;
	private Direction ogDir;
	private Direction dir = Direction.NORTH;
	
	public Coordinates(MapPiece piece, String line) {
		this(piece, line, false);
	}
	
	public Coordinates(MapPiece piece, String line, boolean usesMinecraftCoords) {
		String[] parsed = line.split(",");
		this.x = Integer.parseInt(parsed[0]);
		this.y = Integer.parseInt(parsed[1]);
		this.z = Integer.parseInt(parsed[2]);
		this.xlen = usesMinecraftCoords ? piece.getShape().getBaseLength() * 16 - 1: piece.getShape().getBaseLength() - 1;
		this.zlen = usesMinecraftCoords ? piece.getShape().getBaseHeight() * 16 - 1: piece.getShape().getBaseHeight() - 1;
		this.xp = xlen - x;
		this.zp = zlen - z;
		this.ogDir = parsed.length > 3 ? Direction.getFromCharacter(parsed[3].charAt(0)) : Direction.NORTH;
		this.dir = ogDir;
	}
	
	public Coordinates(int x, int y, int z, int xlen, int zlen) {
		this(x, y, z, xlen, zlen, Direction.NORTH);
	}
	
	public Coordinates(int x, int y, int z, int xlen, int zlen, Direction dir) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.xlen = xlen;
		this.zlen = zlen;
		this.xp = xlen - x;
		this.zp = zlen - z;
		this.ogDir = dir;
		this.dir = dir;
	}
	
	public Coordinates clone() {
		return new Coordinates(x, y, z, xlen, zlen, ogDir);
	}
	
	public Direction getOriginalDirection() {
		return ogDir;
	}
	
	public void setOriginalDirection(Direction dir) {
		ogDir = dir;
	}
	
	public void setDirection(Direction dir) {
		this.dir = dir;
	}
	
	public Direction getDirection() {
		return dir;
	}
	
	@Override
	protected void update() {
		super.update();
		dir = ogDir.rotate(numRotations);
		if (flipX) {
			dir = dir.flip(true);
		}
		if (flipZ) {
			dir = dir.flip(false);
		}
	}
	
	private int[] getCoordinates() {
		int newX = swapAxes ? (reverseX ? zp : z) : (reverseX ? xp : x);
		int newZ = swapAxes ? (reverseZ ? xp : x) : (reverseZ ? zp : z);
		
		return new int[] {newX, newZ};
	}
	
	public int getX() {
		return getCoordinates()[0] + xOff;
	}
	
	public int getY() {
		return y + yOff;
	}
	
	public int getZ() {
		return getCoordinates()[1] + zOff;
	}
	
	public int getXFacing() {
		int offset = 0;
		if (dir == Direction.EAST) offset = 1;
		else if (dir == Direction.WEST) offset = -1;
		return getX() + offset;
	}
	
	public int getZFacing() {
		int offset = 0;
		if (dir == Direction.NORTH) offset = 1;
		else if (dir == Direction.SOUTH) offset = -1;
		return getZ() + offset;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	@Override
	public Coordinates applySettings(MapPieceInstance settings) {
		this.xOff = settings.getX();
		this.yOff = settings.getY();
		this.zOff = settings.getZ();
		super.applySettings(settings);
		return this;
	}
	
	// Should only be used for spawners as it turns x offset into chunk offset
	// * 15 because xOff/zOff was already added once
	public Location toLocation() {
		return new Location(Bukkit.getWorld(Area.WORLD_NAME), getX() + (xOff * 15), getY() - 1, getZ() + (zOff * 15), dir.getYaw(), 0);
	}
	
	@Override
	public String toString() {
		return getX() + "," + getY() + "," + getZ() + "," + dir.getCharacter();
	}
	
	public String toStringFacing() {
		return getX() + "," + getZ() + ":" + getXFacing() + "," + getY() + "," + getZFacing() + ":" + dir.getCharacter();
	}
	
	public boolean isFacing(Coordinates coords) {
		return getX() == coords.getXFacing() && getZ() == coords.getZFacing() && getXFacing() == coords.getX() && getZFacing() == coords.getZ();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dir == null) ? 0 : dir.getValue() + 1);
		result = prime * result + ((ogDir == null) ? 0 : ogDir.getValue() + 1);
		result = prime * result + x;
		result = prime * result + xOff;
		result = prime * result + xlen;
		result = prime * result + xp;
		result = prime * result + y;
		result = prime * result + yOff;
		result = prime * result + z;
		result = prime * result + zOff;
		result = prime * result + zlen;
		result = prime * result + zp;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Coordinates other = (Coordinates) obj;
		if (dir != other.dir) return false;
		if (ogDir != other.ogDir) return false;
		if (x != other.x) return false;
		if (xOff != other.xOff) return false;
		if (xlen != other.xlen) return false;
		if (xp != other.xp) return false;
		if (y != other.y) return false;
		if (yOff != other.yOff) return false;
		if (z != other.z) return false;
		if (zOff != other.zOff) return false;
		if (zlen != other.zlen) return false;
		if (zp != other.zp) return false;
		return true;
	}
}

