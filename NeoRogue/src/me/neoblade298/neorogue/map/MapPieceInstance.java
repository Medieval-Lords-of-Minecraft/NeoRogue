package me.neoblade298.neorogue.map;

public class MapPieceInstance {
	private MapPiece piece;
	private int numRotations;
	private int x, y; // In chunk offset
	private boolean flipX, flipY;
	private MapEntrance available, toAttach;
	public MapPieceInstance(MapPiece piece, int numRotations, boolean flipX, boolean flipY, MapEntrance available, MapEntrance toAttach) {
		this.piece = piece;
		this.numRotations = numRotations;
		this.flipX = flipX;
		this.flipY = flipY;
		this.available = available;
		this.toAttach = toAttach;

		int[] availCoords = available.getCoordinates();
		int[] potentialCoords = toAttach.getChunkCoordinates();
		this.x = availCoords[0] - potentialCoords[0];
		this.y = availCoords[1] - potentialCoords[1];
	}
	public int getNumRotations() {
		return numRotations;
	}
	public boolean isFlipX() {
		return flipX;
	}
	public boolean isFlipY() {
		return flipY;
	}
	
	public MapEntrance getAvailableEntrance() {
		return available;
	}
	
	public MapEntrance getEntranceToAttach() {
		return toAttach;
	}
	
	public MapPiece getPiece() {
		return piece;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (flipX ? 1231 : 1237);
		result = prime * result + (flipY ? 1231 : 1237);
		result = prime * result + numRotations;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		MapPieceInstance other = (MapPieceInstance) obj;
		if (flipX != other.flipX) return false;
		if (flipY != other.flipY) return false;
		if (numRotations != other.numRotations) return false;
		return true;
	}
	
	public void paste() {
		piece.applySettings(this);
		piece.paste(x * 16, y * 16);
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
}
