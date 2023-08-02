package me.neoblade298.neorogue.map;

public class MapPiece {
	private MapShape shape;
	private MapEntrance[] entrances;
	
	public int getNumEntrances() {
		return entrances.length;
	}
	
	public MapShape getShape() {
		return shape;
	}
}
