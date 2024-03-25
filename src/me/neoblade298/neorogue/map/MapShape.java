package me.neoblade298.neorogue.map;

import java.util.List;

public class MapShape extends Rotatable {
	private boolean[][] shape;
	private int xlen, zlen;
	private int chunkCount;
	
	public MapShape(List<String> lines) {
		xlen = lines.get(0).length() - 1;
		zlen = lines.size() - 1;
		chunkCount = 0;
		this.shape = new boolean[xlen + 1][zlen + 1];
		for (int z = 0; z <= zlen; z++) {
			String line = lines.get(lines.size() - z - 1);
			for (int x = 0; x <= xlen; x++) {
				if (line.charAt(x) == 'X') {
					shape[x][z] = true;
					chunkCount++;
				}
			}
		}
	}
	
	public MapShape(boolean[][] shape) {
		this.shape = shape;
		xlen = shape.length - 1;
		zlen = shape[0].length - 1;
		
		for (int x = 0; x <= xlen; x++) {
			for (int z = 0; z <= zlen; z++) {
				if (shape[x][z])
					chunkCount++;
			}
		}
	}
	
	@Override
	public MapShape clone() {
		return new MapShape(this.shape);
	}
	
	public boolean get(int x, int z) {
		int[] coords = getCoordinates(x, z);
		return shape[coords[0]][coords[1]];
	}
	
	// returns in x, z form
	public int[] getCoordinates(int x, int z) {
		int xp = (swapAxes ? zlen : xlen) - x, zp = (swapAxes ? xlen : zlen) - z;
		int newX = swapAxes ? (reverseX ? zp : z) : (reverseX ? xp : x);
		int newZ = swapAxes ? (reverseZ ? xp : x) : (reverseZ ? zp : z);
		
		return new int[] { newX, newZ };
	}
	
	@Override
	public Rotatable applySettings(MapPieceInstance settings) {
		this.setRotations(settings.numRotations);
		this.setFlip(settings.flipX, settings.flipZ);
		return this;
	}
	
	// Needs a special update because we're rotating the camera instead of the actual coords
	@Override
	protected void update() {
		if (flipX && flipZ) {
			flipX = false;
			flipZ = false;
			numRotations = (numRotations + 2) % 4;
			update();
			return;
		}
		
		swapAxes = numRotations % 2 == 1;
		switch (numRotations) {
		case 0:
			reverseX = false;
			reverseZ = false;
			break;
		case 1:
			reverseX = true;
			reverseZ = false;
			break;
		case 2:
			reverseX = true;
			reverseZ = true;
			break;
		case 3:
			reverseX = false;
			reverseZ = true;
			break;
		}
		
		if (flipZ) {
			if (numRotations % 2 == 0) {
				reverseX = !reverseX;
			} else {
				reverseZ = !reverseZ;
			}
		} else if (flipX) {
			if (numRotations % 2 == 0) {
				reverseZ = !reverseZ;
			} else {
				reverseX = !reverseX;
			}
		}
	}
	
	public void display() {
		for (int z = getHeight() - 1; z >= 0; z--) {
			for (int x = 0; x < getLength(); x++) {
				System.out.print((get(x, z) ? 'X' : '_'));
				get(x, z);
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

	public int getChunkCount() {
		return chunkCount;
	}
}
