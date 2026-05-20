package me.neoblade298.neorogue.map;

public abstract class Rotatable {
	protected int numRotations = 0, xlen, zlen;
	protected boolean flipX, flipZ;
	protected boolean reverseX, reverseZ, swapAxes;
	
	protected void update() {
		reverseX = numRotations % 3 != 0;
		reverseZ = numRotations >= 2;
		
		if (flipX && flipZ) {
			flipX = false;
			flipZ = false;
			numRotations = (numRotations + 2) % 4;
			update();
			return;
		}
		if (flipX) {
			reverseZ = !reverseZ;
		}
		if (flipZ) {
			reverseX = !reverseX;
		}
		swapAxes = numRotations % 2 == 1;
		if (swapAxes) {
			reverseX = !reverseX;
			reverseZ = !reverseZ;
		}
	}
	
	public Rotatable applySettings(MapPieceInstance settings) {
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
	
	public void setFlip(boolean flipX, boolean flipZ) {
		this.flipX = flipX;
		this.flipZ = flipZ;
		update();
	}
}
