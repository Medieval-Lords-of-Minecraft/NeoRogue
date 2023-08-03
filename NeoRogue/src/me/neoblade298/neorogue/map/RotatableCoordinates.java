package me.neoblade298.neorogue.map;


/* Assumed to always be rotating around 0,0 origin
 * After rotating, the coordinates translate themselves to be above 0,0
 */
public class RotatableCoordinates {
	private int origX, origY, numRotations = 0, length, height;
	
	private boolean reverseX, reverseY, flipX, flipY;
	
	public RotatableCoordinates(int x, int y, MapPiece piece) {
		this.origX = x;
		this.origY = y;
		this.length = piece.getShape().getLength() * 16;
		this.height = piece.getShape().getHeight() * 16;
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
	}
	
	public int getX() {
		return flipX ? length - origX : origX;
	}
	
	public int getY() {
		return flipY ? length - origY : origY;
	}
}
