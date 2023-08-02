package me.neoblade298.neorogue.map;

import org.bukkit.Bukkit;

public class MapShape {
	private boolean[][] shape;
	private int numRotations = 0;
	private boolean flipX, flipY;
	
	private int xlen, ylen;
	private boolean reverseX, reverseY, swapAxes;
	
	public MapShape(boolean[][] shape) {
		this.shape = shape;
		xlen = shape.length - 1;
		ylen = shape[0].length - 1;
		System.out.println("xlen: " + xlen + ", " + "ylen: " + ylen);
	}
	
	public boolean get(int x, int y) {
		int newX = reverseX ? (!swapAxes ? ylen : xlen) - x : x;
		int newY = reverseY ? (!swapAxes ? xlen : ylen) - y : y;
		
		try {
			return swapAxes ? shape[newX][newY] : shape[newY][newX];
		}
		catch (Exception e) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to retrieve coordinates " + x + "," + y + " from MapShape");
		}
		return false;
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
