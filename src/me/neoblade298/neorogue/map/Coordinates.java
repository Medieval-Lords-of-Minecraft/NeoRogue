package me.neoblade298.neorogue.map;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import me.neoblade298.neorogue.area.Area;

/* Assumed to always be rotating around 0,0 origin
 * After rotating, the coordinates translate themselves to be above 0,0
 * Used for MapEntrances and MapSpawners, MapEntrance uses direction too
 */
public class Coordinates extends Rotatable {
	private double x, y, z, xp, zp, xlen, zlen, xOff, yOff, zOff;
	private Direction ogDir;
	private Direction dir = Direction.NORTH;
	
	public Coordinates(MapPiece piece, String line) {
		this(piece, line, false);
	}
	
	public Coordinates(MapPiece piece, String line, boolean usesMinecraftCoords) {
		String[] parsed = line.split(",");
		this.x = Double.parseDouble(parsed[0]);
		this.y = Double.parseDouble(parsed[1]);
		this.z = Double.parseDouble(parsed[2]);
		this.xlen = usesMinecraftCoords ? piece.getShape().getBaseLength() * 16 - 1: piece.getShape().getBaseLength() - 1;
		this.zlen = usesMinecraftCoords ? piece.getShape().getBaseHeight() * 16 - 1: piece.getShape().getBaseHeight() - 1;
		this.xp = xlen - x;
		this.zp = zlen - z;
		this.ogDir = parsed.length > 3 ? Direction.getFromCharacter(parsed[3].charAt(0)) : Direction.NORTH;
		this.dir = ogDir;
	}
	
	public Coordinates(double x, double y, double z, double xlen, double zlen) {
		this(x, y, z, xlen, zlen, Direction.NORTH, Direction.NORTH);
	}
	
	public Coordinates(double x, double y, double z, double xlen, double zlen, Direction ogDir, Direction dir) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.xlen = xlen;
		this.zlen = zlen;
		this.xp = xlen - x;
		this.zp = zlen - z;
		this.ogDir = ogDir;
		this.dir = dir;
	}
	
	public Coordinates(double x, double y, double z, double xlen, double zlen, Direction ogDir, Direction dir, double xOff, double yOff, double zOff, int numRotations,
			boolean flipX, boolean flipZ) {
		this(x, y, z, xlen, zlen, ogDir, dir);
		this.xOff = xOff;
		this.yOff = yOff;
		this.zOff = zOff;
		update();
	}
	
	public Coordinates clone() {
		return new Coordinates(x, y, z, xlen, zlen, ogDir, dir, xOff, yOff, zOff, numRotations, flipX, flipZ);
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
	
	private double[] getCoordinates() {
		double newX = swapAxes ? (reverseX ? zp : z) : (reverseX ? xp : x);
		double newZ = swapAxes ? (reverseZ ? xp : x) : (reverseZ ? zp : z);
		
		return new double[] {newX, newZ};
	}
	
	public double getX() {
		return getCoordinates()[0] + xOff;
	}
	
	public double getY() {
		return y + yOff;
	}
	
	public double getZ() {
		return getCoordinates()[1] + zOff;
	}
	
	public double getXFacing() {
		int offset = 0;
		if (dir == Direction.EAST) offset = 1;
		else if (dir == Direction.WEST) offset = -1;
		return getX() + offset;
	}
	
	public double getZFacing() {
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
	public Location toBlockLocation() {
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
	
	public boolean canConnect(Coordinates other) {
		if (other.getY() != getY()) return false;
		return isFacing(other) && dir.equals(other.dir.invert());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ogDir == null) ? 0 : ogDir.hashCode());
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(xOff);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(xlen);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yOff);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(z);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(zOff);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(zlen);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Coordinates other = (Coordinates) obj;
		if (dir != other.dir) return false;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) return false;
		if (Double.doubleToLongBits(xOff) != Double.doubleToLongBits(other.xOff)) return false;
		if (Double.doubleToLongBits(xlen) != Double.doubleToLongBits(other.xlen)) return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y)) return false;
		if (Double.doubleToLongBits(yOff) != Double.doubleToLongBits(other.yOff)) return false;
		if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z)) return false;
		if (Double.doubleToLongBits(zOff) != Double.doubleToLongBits(other.zOff)) return false;
		if (Double.doubleToLongBits(zlen) != Double.doubleToLongBits(other.zlen)) return false;
		return true;
	}
}

