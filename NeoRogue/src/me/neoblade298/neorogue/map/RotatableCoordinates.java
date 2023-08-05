package me.neoblade298.neorogue.map;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import me.neoblade298.neorogue.area.Area;

/* Assumed to always be rotating around 0,0 origin
 * After rotating, the coordinates translate themselves to be above 0,0
 */
public class RotatableCoordinates {
	private int x, y, z, numRotations = 0, xlen, zlen;
	private Direction dir = Direction.NORTH;
	
	private boolean reverseX, reverseY, flipX, flipY;
	
	public RotatableCoordinates(MapPiece piece, String line) {
		String[] parsed = line.split(",");
		this.x = Integer.parseInt(parsed[0]);
		this.y = Integer.parseInt(parsed[1]);
		this.z = Integer.parseInt(parsed[2]);
		this.xlen = piece.getShape().getLength() - 1;
		this.zlen = piece.getShape().getHeight() - 1;
		this.dir = Direction.getFromCharacter(parsed[2].charAt(0));
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
		return new RotatableCoordinates(x, y, z, xlen, zlen);
	}
	
	public Direction getDirection() {
		return dir;
	}
	
	public void rotate(int times) {
		numRotations += times;
		numRotations %= 4;
		if (dir != null) dir = dir.rotate(times);
		update();
	}
	
	public void flip(boolean xAxis) {
		if (xAxis) {
			flipX = !flipX;
			dir = dir.flip(true);
		}
		else {
			flipY = !flipY;
			dir = dir.flip(false);
		}
		update();
	}
	
	private void update() {
		reverseY = numRotations % 3 != 0;
		reverseX = numRotations >= 2;
		
		if (flipX && flipY) {
			flipX = false;
			flipY = false;
			rotate(2);
			return;
		}
		if (flipX) reverseX = !reverseX;
		if (flipY) reverseY = !reverseY;
	}
	
	public int getX() {
		return flipX ? xlen - x : x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getZ() {
		return flipY ? zlen - z : z;
	}
	
	public RotatableCoordinates applySettings(MapPieceInstance settings) {
		this.numRotations = settings.numRotations;
		this.flipX = settings.flipX;
		this.flipY = settings.flipY;
		update();
		return this;
	}
	
	public Location toLocation() {
		return new Location(Bukkit.getWorld(Area.WORLD_NAME), getX(), getY(), getZ());
	}
}
