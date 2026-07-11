package me.neoblade298.neorogue.map;

import java.util.Collections;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import me.neoblade298.neorogue.region.Region;

/* Assumed to always be rotating around 0,0 origin
 * After rotating, the coordinates translate themselves to be above 0,0
 * Used for point-based map data like spawners and mythic locations.
 */
public class Coordinates extends Rotatable {
	private double x, y, z, xp, zp, xlen, zlen, xOff, yOff, zOff;
	private boolean minecraftCoords;
	private Direction ogDir;
	private Direction dir = Direction.NORTH;
	private HashSet<String> tags = new HashSet<>();
	private HashSet<String> requiredTags = new HashSet<>();
	private HashSet<String> allowedTags = new HashSet<>();
	
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
		this.minecraftCoords = usesMinecraftCoords;
		computePrime();
		this.ogDir = parsed.length > 3 ? Direction.getFromCharacter(parsed[3].charAt(0)) : Direction.NORTH;
		this.dir = ogDir;
		if (parsed.length > 4) this.tags = parseTags(parsed[4]);
		if (parsed.length > 5) this.requiredTags = parseTags(parsed[5]);
		if (parsed.length > 6) this.allowedTags = parseTags(parsed[6]);
	}
	
	private static HashSet<String> parseTags(String str) {
		HashSet<String> result = new HashSet<>();
		if (str != null && !str.isEmpty()) {
			for (String tag : str.split("\\+")) {
				if (!tag.isEmpty()) result.add(tag);
			}
		}
		return result;
	}
	
	// Reverses a coordinate for rotation/flip. Half-block (centered) points shift by 1 to stay within
	// the mirrored span; whole-number points keep the plain index reversal and are fenceposted later.
	private void computePrime() {
		this.xp = x % 1 != 0 ? xlen + 1 - x : xlen - x;
		this.zp = z % 1 != 0 ? zlen + 1 - z : zlen - z;
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
		computePrime();
		this.ogDir = ogDir;
		this.dir = dir;
	}
	
	public Coordinates(double x, double y, double z, double xlen, double zlen, Direction ogDir, Direction dir, double xOff, double yOff, double zOff, int numRotations,
			boolean flipX, boolean flipZ, boolean minecraftCoords) {
		this(x, y, z, xlen, zlen, ogDir, dir);
		this.minecraftCoords = minecraftCoords;
		computePrime();
		this.xOff = xOff;
		this.yOff = yOff;
		this.zOff = zOff;
		update();
	}
	
	public Coordinates clone() {
		Coordinates c = new Coordinates(x, y, z, xlen, zlen, ogDir, dir, xOff, yOff, zOff, numRotations, flipX, flipZ, minecraftCoords);
		c.tags = new HashSet<>(tags);
		c.requiredTags = new HashSet<>(requiredTags);
		c.allowedTags = new HashSet<>(allowedTags);
		return c;
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

		if (minecraftCoords) {
			// Whole-number coordinates sit on a block boundary, so a rotation that reverses an axis
			// leaves them a block short of where a centered (x.5) coordinate would land. Nudge the
			// reversed axis back by one block. The pre-swap reverse flags identify which world axis
			// was reversed; world X is negated downstream (so subtract) while world Z is not (so add).
			boolean preRevX = swapAxes ? !reverseX : reverseX;
			boolean preRevZ = swapAxes ? !reverseZ : reverseZ;
			boolean newXWhole = (swapAxes ? z : x) % 1 == 0;
			boolean newZWhole = (swapAxes ? x : z) % 1 == 0;
			if (preRevX && newXWhole) newX -= 1;
			if (preRevZ && newZWhole) newZ += 1;
		}

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
		return new Location(Bukkit.getWorld(Region.getActiveWorldName()), getX() + (xOff * 15), getY() - 1, getZ() + (zOff * 15), dir.getYaw(), 0);
	}
	public Location toBlockLocation() {
		return new Location(Bukkit.getWorld(Region.getActiveWorldName()), getX() + (xOff * 15), getY() - 1, getZ() + (zOff * 15), dir.getYaw(), 0);
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
		if (!isFacing(other) || !dir.equals(other.dir.invert())) return false;
		// Check this entrance's tag requirements against the other entrance's tags
		if (!other.tags.containsAll(requiredTags)) return false;
		if (!allowedTags.isEmpty() && Collections.disjoint(allowedTags, other.tags)) return false;
		// Check the other entrance's tag requirements against this entrance's tags
		if (!tags.containsAll(other.requiredTags)) return false;
		if (!other.allowedTags.isEmpty() && Collections.disjoint(other.allowedTags, tags)) return false;
		return true;
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

