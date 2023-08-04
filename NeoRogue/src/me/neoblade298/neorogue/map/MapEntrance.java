package me.neoblade298.neorogue.map;

public class MapEntrance {
	private MapPiece piece;
	private Direction face;
	private int x, y;
	
	public MapEntrance(MapPiece piece, String line) {
		this.piece = piece;
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
	public int[] getCoordinates() {
		int[] coords = piece.getShape().getCoordinates(x, y);
		switch (face) {
		case NORTH: coords[1] += 1;
		case EAST: coords[0] += 1;
		case SOUTH: coords[1] -= 1;
		case WEST: coords[0] -= 1;
		}
		return coords;
	}
	public int[] getChunkCoordinates() {
		return piece.getShape().getCoordinates(x, y);
	}
	public void rotate(int amount) {
		this.face = this.face.rotate(amount);
	}
	public void flip(boolean xAxis) {
		this.face = this.face.flip(xAxis);
	}
	
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((face == null) ? 0 : face.hashCode());
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}
        
    @Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		MapEntrance other = (MapEntrance) obj;
		if (face != other.face) return false;
		if (x != other.x) return false;
		if (y != other.y) return false;
		return true;
	}
}
