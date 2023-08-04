package me.neoblade298.neorogue.map;

public class MapPieceSettings {
	private int numRotations;
	private boolean flipX, flipY;
	private MapEntrance available, toAttach;
	public MapPieceSettings(int numRotations, boolean flipX, boolean flipY, MapEntrance available, MapEntrance toAttach) {
		this.numRotations = numRotations;
		this.flipX = flipX;
		this.flipY = flipY;
		this.available = available;
		this.toAttach = toAttach;
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
		MapPieceSettings other = (MapPieceSettings) obj;
		if (flipX != other.flipX) return false;
		if (flipY != other.flipY) return false;
		if (numRotations != other.numRotations) return false;
		return true;
	}
}
