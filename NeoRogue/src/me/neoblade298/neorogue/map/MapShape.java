package me.neoblade298.neorogue.map;

import java.util.List;

import org.bukkit.Bukkit;

public class MapShape {
	private boolean[][] shape;
	private int numRotations = 0;
	private boolean flipX, flipZ;
	
	private int xlen, zlen;
	private boolean reverseX, reverseZ, swapAxes;
	
	public MapShape(List<String> lines) {
		this.shape = new boolean[lines.size()][lines.get(0).length()];
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			int j = 0;
			for (char c : line.toCharArray()) {
				shape[lines.size() - i - 1][j++] = c == 'X';
			}
		}
		xlen = shape.length - 1;
		zlen = shape[0].length - 1;
	}
	
	public MapShape(boolean[][] shape) {
		this.shape = shape;
		xlen = shape.length - 1;
		zlen = shape[0].length - 1;
	}
	
	public boolean get(int x, int z) {
		int newX = reverseX ? (!swapAxes ? zlen : xlen) - x : x;
		int newZ = reverseZ ? (!swapAxes ? xlen : zlen) - z : z;
		
		try {
			return swapAxes ? shape[newX][newZ] : shape[newZ][newX]; // X is swapped with y because of how arrays are
		}
		catch (Exception e) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to retrieve coordinates " + x + "," + z + " from MapShape");
		}
		return false;
	}
	
	// returns in x, z form
	public int[] getCoordinates(int x, int z) {
		int newX = reverseX ? (!swapAxes ? zlen : xlen) - x : x;
		int newZ = reverseZ ? (!swapAxes ? xlen : zlen) - z : z;
		
		return swapAxes ? new int[] {newZ, newX} : new int[] {newX, newZ};
	}
	
	private void update() {
		reverseZ = numRotations % 3 != 0;
		reverseX = numRotations >= 2;
		
		if (flipX && flipZ) {
			flipX = false;
			flipZ = false;
			numRotations = (numRotations + 2) % 4;
			update();
			return;
		}
		if (flipX) reverseX = !reverseX; // Intentionally different from rotatable coordinates
		if (flipZ) reverseZ = !reverseZ;
		swapAxes = numRotations % 2 == 1;
		if (swapAxes) {
			reverseX = !reverseX;
			reverseZ = !reverseZ;
		}
	}
	
	public void display() {
		for (int i = getHeight() - 1; i >= 0; i--) {
			System.out.print("[");
			for (int j = 0; j < getLength(); j++) {
				System.out.print(get(i, j) + " ");
				//System.out.print("Get " + i + ", " + j + ": ");
				//get(i,j);
			}
			System.out.println("]");
		}
		System.out.println();
	}
	
	public int getHeight() {
		return !swapAxes ? shape.length : shape[0].length;
	}
	
	public int getBaseHeight() {
		return shape.length;
	}
	
	public int getLength() {
		return !swapAxes ? shape[0].length : shape.length;
	}
	
	public int getBaseLength() {
		return shape[0].length;
	}
	
	public boolean[][] getBaseShape() {
		int length = getLength();
		int height = getHeight();
		boolean[][] baseShape = new boolean[length][height];
		for (int x = 0; x < getLength(); x++) {
			for (int y = 0; y < getHeight(); y++) {
				baseShape[x][y] = get(x, y);
			}
		}
		return baseShape;
	}
	
	public void applySettings(MapPieceInstance settings) {
		this.numRotations = settings.numRotations;
		this.flipX = settings.flipX;
		this.flipZ = settings.flipZ;
		update();
	}
	
	public void setRotations(int amount) {
		this.numRotations = amount;
		update();
	}
	
	public void setFlip(boolean flipX, boolean flipZ) {
		this.flipX = flipX;
		this.flipZ = flipZ;
		update();
	}
}
