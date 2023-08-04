package me.neoblade298.neorogue.map;

import java.util.List;

import org.bukkit.Bukkit;

public class MapShape {
	private boolean[][] shape;
	private int numRotations = 0;
	private boolean flipX, flipY;
	
	private int xlen, ylen;
	private boolean reverseX, reverseY, swapAxes;
	
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
		ylen = shape[0].length - 1;
	}
	
	public MapShape(boolean[][] shape) {
		this.shape = shape;
		xlen = shape.length - 1;
		ylen = shape[0].length - 1;
	}
	
	public boolean get(int x, int y) {
		int newX = reverseX ? (!swapAxes ? ylen : xlen) - x : x;
		int newY = reverseY ? (!swapAxes ? xlen : ylen) - y : y;
		
		try {
			return swapAxes ? shape[newX][newY] : shape[newY][newX]; // X is swapped with y because of how arrays are
		}
		catch (Exception e) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to retrieve coordinates " + x + "," + y + " from MapShape");
		}
		return false;
	}
	
	// returns in x, y form
	public int[] getCoordinates(int x, int y) {
		int newX = reverseX ? (!swapAxes ? ylen : xlen) - x : x;
		int newY = reverseY ? (!swapAxes ? xlen : ylen) - y : y;
		
		return swapAxes ? new int[] {newY, newX} : new int[] {newX, newY};
	}
	
	public void rotate(int times) {
		numRotations += times;
		numRotations %= 4;
		update();
	}
	
	public void flip(boolean xAxis) {
		if (xAxis) flipX = !flipX;
		else flipY = !flipY;
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
		swapAxes = numRotations % 2 == 1;
		if (swapAxes) {
			reverseX = !reverseX;
			reverseY = !reverseY;
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
		return swapAxes ? shape.length : shape[0].length;
	}
	
	public int getLength() {
		return swapAxes ? shape[0].length : shape.length;
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
}
