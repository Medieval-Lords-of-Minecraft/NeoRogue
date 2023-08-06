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
	private final Direction ogDir;
	private Direction dir = Direction.NORTH;
	
	private boolean reverseX, reverseZ, flipX, flipZ, swapAxes;
	
	public RotatableCoordinates(MapPiece piece, String line) {
		String[] parsed = line.split(",");
		this.x = Integer.parseInt(parsed[0]);
		this.y = Integer.parseInt(parsed[1]);
		this.z = Integer.parseInt(parsed[2]);
		this.xlen = piece.getShape().getLength() - 1;
		this.zlen = piece.getShape().getHeight() - 1;
		this.ogDir = Direction.getFromCharacter(parsed[3].charAt(0));
		this.dir = ogDir;
	}
	
	public RotatableCoordinates(int x, int y, int z, int xlen, int zlen) {
		this(x, y, z, xlen, zlen, Direction.NORTH);
	}
	
	public RotatableCoordinates(int x, int y, int z, int xlen, int zlen, Direction dir) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.xlen = xlen;
		this.zlen = zlen;
		this.ogDir = dir;
		this.dir = dir;
	}
	
	public RotatableCoordinates clone() {
		return new RotatableCoordinates(x, y, z, xlen, zlen, ogDir);
	}
	
	public Direction getOriginalDirection() {
		return ogDir;
	}
	
	public Direction getDirection() {
		return dir;
	}
	
	private void update() {
		dir = ogDir.rotate(numRotations);
		reverseZ = numRotations % 3 != 0;
		reverseX = numRotations >= 2;
		
		if (flipX && flipZ) {
			flipX = false;
			flipZ = false;
			numRotations = (numRotations + 2) % 4;
			update();
			return;
		}
		if (flipX) {
			reverseZ = !reverseZ;
			dir = dir.flip(true);
		}
		if (flipZ) {
			reverseX = !reverseX;
			dir = dir.flip(false);
		}
		swapAxes = numRotations % 2 == 1;
		if (swapAxes) {
			reverseX = !reverseX;
			reverseZ = !reverseZ;
		}
	}
	
	private int[] getCoordinates() {
		int newX = reverseX ? (!swapAxes ? zlen : xlen) - x : x;
		int newZ = reverseZ ? (!swapAxes ? xlen : zlen) - z : z;
		
		return swapAxes ? new int[] {newZ, newX} : new int[] {newX, newZ};
	}
	
	public int getX() {
		return getCoordinates()[0] + xOff;
	}
	
	public int getY() {
		return y;
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
	
	public RotatableCoordinates applySettings(MapPieceInstance settings) {
		this.xOff = settings.getX();
		this.yOff = settings.getY();
		this.zOff = settings.getZ();
		this.numRotations = settings.numRotations;
		this.flipX = settings.flipX;
		this.flipZ = settings.flipZ;
		update();
		return this;
	}
	
	public void setRotations(int amount) {
		this.numRotations = amount;
		update();
	}
	
	public void setFlipX(boolean flipX) {
		this.flipX = flipX;
		update();
	}
	
	public void setFlipZ(boolean flipZ) {
		this.flipZ = flipZ;
		update();
	}
	
	public Location toLocation() {
		return new Location(Bukkit.getWorld(Area.WORLD_NAME), getX(), getY(), getZ());
	}
	
	@Override
	public String toString() {
		return getX() + "," + getY() + "," + getZ() + "," + dir.getCharacter();
	}
	
	public String toStringFacing() {
		return getX() + "," + getZ() + ":" + getXFacing() + "," + getY() + "," + getZFacing() + ":" + dir.getCharacter();
	}
}
