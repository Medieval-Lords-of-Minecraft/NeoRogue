package me.neoblade298.neorogue.map;

public class MapEntrance {
	Direction face;
	int x, y;
	
	public MapEntrance(String line) {
		String[] parsed = line.split(",");
		this.x = Integer.parseInt(parsed[0]);
		this.y = Integer.parseInt(parsed[1]);
		this.face = Direction.getFromCharacter(parsed[2].charAt(0));
	}
	public MapEntrance(Direction face, int x, int y) {
		this.face = face;
		this.x = x;
		this.y = y;
	}
	public Direction getFace() {
		return face;
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
}
