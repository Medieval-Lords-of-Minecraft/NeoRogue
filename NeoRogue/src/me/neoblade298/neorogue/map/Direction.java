package me.neoblade298.neorogue.map;

/* Confusing, but Direction is INVERTED with actual Minecraft direction
 * North is south, east is west, etc.
 */
public enum Direction {
	NORTH(0, 'N', 0),
	EAST(1, 'E', 90),
	SOUTH(2, 'S', 180),
	WEST(3, 'W', 270);
	
	private int value;
	private char c;
	private float yaw;
	private Direction(int value, char c, float yaw) {
		this.value = value;
		this.c = c;
		this.yaw = yaw;
	}
	
	public char getCharacter() {
		return c;
	}
	
	public Direction rotate(int times) {
		int newVal = (this.value + times) % 4;
		return getFromValue(newVal);
	}
	
	public Direction flip(boolean xAxis) {
		if ((value % 2 == 0 && xAxis) || (value % 2 == 1 && !xAxis)) {
			return invert();
		}
		return this;
	}
	
	public Direction invert() {
		return getFromValue((value + 2) % 4);
	}
	
	public int getValue() {
		return value;
	}
	
	private Direction getFromValue(int value) {
		switch (value) {
		case 0: return NORTH;
		case 1: return EAST;
		case 2: return SOUTH;
		default: return WEST;
		}
	}
	
	public static Direction getFromCharacter(char c) {
		switch (c) {
		case 'N': return NORTH;
		case 'E': return EAST;
		case 'S': return SOUTH;
		default: return WEST;
		}
	}
	
	public float getYaw() {
		return yaw;
	}
}
