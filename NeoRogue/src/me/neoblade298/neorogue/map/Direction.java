package me.neoblade298.neorogue.map;

public enum Direction {
	NORTH(0, 'N'),
	EAST(1, 'E'),
	SOUTH(2, 'S'),
	WEST(3, 'W');
	
	private int value;
	private char c;
	private Direction(int value, char c) {
		this.value = value;
		this.c = c;
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
			return getFromValue((value + 2) % 4);
		}
		return this;
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
}
