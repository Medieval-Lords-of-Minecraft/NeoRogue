package me.neoblade298.neorogue.map;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import me.neoblade298.neorogue.area.Area;

/* Assumed to always be rotating around 0,0 origin
 * After rotating, the coordinates translate themselves to be above 0,0
 * Used for MapEntrances and MapSpawners, MapEntrance uses direction too
 */
public class RotatableCoordinates {
	private int x, y, z, numRotations = 0, xlen, zlen;
	private int xOff, yOff, zOff;
	private Direction dir = Direction.NORTH;
	
	private boolean reverseX, reverseY, flipX, flipY;
	
	public RotatableCoordinates(MapPiece piece, String line) {
		String[] parsed = line.split(",");
		this.x = Integer.parseInt(parsed[0]);
		this.y = Integer.parseInt(parsed[1]);
		this.z = Integer.parseInt(parsed[2]);
		this.xlen = piece.getShape().getLength() - 1;
		this.zlen = piece.getShape().getHeight() - 1;
		this.dir = Direction.getFromCharacter(parsed[3].charAt(0));
	}
	
	public RotatableCoordinates(int x, int y, int z, int xlen, int zlen) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.xlen = xlen;
		this.zlen = zlen;
	}
	
	public RotatableCoordinates(int x, int y, int z, int xlen, int zlen, Direction dir) {
		this(x, y, z, xlen, zlen);
		this.dir = dir;
	}
	
	public RotatableCoordinates clone() {
		return new RotatableCoordinates(x, y, z, xlen, zlen, dir);
	}
	
	public Direction getDirection() {
		return dir;
	}
	
	private void update() {
		dir = dir.rotate(numRotations);
		reverseY = numRotations % 3 != 0;
		reverseX = numRotations >= 2;
		
		if (flipX && flipY) {
			flipX = false;
			flipY = false;
			numRotations = (numRotations + 2) % 4;
			update();
			return;
		}
		
		if (flipX) reverseX = !reverseX;
		if (flipY) reverseY = !reverseY;
	}
	
	public int getX() {
		return (flipX ? xlen - x : x) + xOff;
	}
	
	public int getY() {
		return y;
	}
	
	public int getZ() {
		return (flipY ? zlen - z : z) + zOff;
	}
	
	public int getXFacing() {
		int offset = 0;
		if (dir == Direction.EAST) offset = -1;
		else if (dir == Direction.WEST) offset = 1;
		return getX() + offset;
	}
	
	public int getZFacing() {
		int offset = 0;
		if (dir == Direction.NORTH) offset = 1;
		else if (dir == Direction.SOUTH) offset = -1;
		return getZ() + offset;
	}
	
	public RotatableCoordinates applySettings(MapPieceInstance settings) {
		this.xOff = settings.getX();
		this.yOff = settings.getY();
		this.zOff = settings.getZ();
		this.numRotations = settings.numRotations;
		this.flipX = settings.flipX;
		this.flipY = settings.flipY;
		update();
		return this;
	}
	
	public Location toLocation() {
		return new Location(Bukkit.getWorld(Area.WORLD_NAME), getX(), getY(), getZ());
	}
	
	@Override
	public String toString() {
		return getX() + "," + getY() + "," + getZ() + "," + dir.getCharacter();
	}
	
	public String toStringFacing() {
		return getXFacing() + "," + getY() + "," + getZFacing() + "," + dir.getCharacter();
	}
}
