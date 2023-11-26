package me.neoblade298.neorogue.map;

import java.util.List;

public class MapShape extends Rotatable {
	private boolean[][] shape;
	private int xlen, zlen;
	
	public MapShape(List<String> lines) {
		xlen = lines.get(0).length() - 1;
		zlen = lines.size() - 1;
		this.shape = new boolean[xlen + 1][zlen + 1];
		for (int z = 0; z <= zlen; z++) {
			String line = lines.get(lines.size() - z - 1);
			for (int x = 0; x <= xlen; x++) {
				shape[x][z] = line.charAt(x) == 'X';
			}
		}
	}
	
	public MapShape(boolean[][] shape) {
		this.shape = shape;
		xlen = shape.length - 1;
		zlen = shape[0].length - 1;
	}
	
	public MapShape clone() {
		return new MapShape(this.shape);
	}
	
	public boolean get(int x, int z) {
		int[] coords = getCoordinates(x, z);
		return shape[coords[0]][coords[1]];
		/*int newX = reverseX ? (!swapAxes ? zlen : xlen) - x : x;
		int newZ = reverseZ ? (!swapAxes ? xlen : zlen) - z : z;
		
		try {
			return swapAxes ? shape[newX][newZ] : shape[newZ][newX]; // X is swapped with y because of how arrays are
		}
		catch (Exception e) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to retrieve coordinates " + x + "," + z + " from MapShape");
		}
		return false;*/
	}
	
	// returns in x, z form
	public int[] getCoordinates(int x, int z) {
		int xp = (swapAxes ? zlen : xlen) - x, zp = (swapAxes ? xlen : zlen) - z;
		int newX = swapAxes ? (reverseX ? zp : z) : (reverseX ? xp : x);
		int newZ = swapAxes ? (reverseZ ? xp : x) : (reverseZ ? zp : z);
		
		return new int[] {newX, newZ};
	}
	
	
	/* This must be reversed because we're rotating where we're getting the coordinates from,
	 * not where the coordinates are. To make an object on camera rotate right, we must rotate
	 * the camera left.
	 */
	@Override
	public void setRotations(int amount) {
		super.setRotations((4 - amount) % 4);
	}
	
	@Override
	public Rotatable applySettings(MapPieceInstance settings) {
		this.numRotations = 4 - (settings.numRotations % 4);
		this.flipX = settings.flipZ;
		this.flipZ = settings.flipX;
		update();
		return this;
	}
	
	@Override
	public void setFlip(boolean flipX, boolean flipZ) {
		this.flipX = flipZ;
		this.flipZ = flipX;
		update();
	}
	
	public void display() {
		for (int z = getHeight() - 1; z >= 0; z--) {
			for (int x = 0; x < getLength(); x++) {
				System.out.print((get(x, z) ? 'X' : '_'));
				get(x,z);
			}
			System.out.println();
		}
	}
	
	public int getHeight() {
		return swapAxes ? shape.length : shape[0].length;
	}
	
	public int getBaseHeight() {
		return shape[0].length;
	}
	
	public int getLength() {
		return swapAxes ? shape[0].length : shape.length;
	}
	
	public int getBaseLength() {
		return shape.length;
	}
}
