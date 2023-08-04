package me.neoblade298.neorogue.map;

public class MapEntrance {
	private Direction face;
	private int x, y, z, origX, origZ; // x and z are chunk coordinates, y is just coordinates
	
	public MapEntrance(MapPiece piece, String line) {
		String[] parsed = line.split(",");
		this.x = Integer.parseInt(parsed[0]);
		this.z = Integer.parseInt(parsed[1]);
		this.face = Direction.getFromCharacter(parsed[2].charAt(0));
	}
	public MapEntrance(Direction face, int x, int y, int z) {
		this.face = face;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public Direction getFace() {
		return face;
	}
	// Chunk coordinates using settings
	public int[] getCoordinates(MapPieceInstance inst) {
		int[] coords = inst.getPiece().getShape().getCoordinates(x, z);
		switch (face) {
		case NORTH: coords[1] += 1;
		case EAST: coords[0] += 1;
		case SOUTH: coords[1] -= 1;
		case WEST: coords[0] -= 1;
		}
		return coords;
	}
	public int[] getChunkCoordinates(MapPieceInstance inst) {
		return piece.getShape().getCoordinates(x, z);
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
		result = prime * result + z;
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
		if (z != other.z) return false;
		return true;
	}
    
    public int getY() {
    	return y;
    }
}
