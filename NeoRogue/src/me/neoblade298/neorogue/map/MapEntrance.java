package me.neoblade298.neorogue.map;

public class MapEntrance {
	Direction face;
	int x, y;
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
