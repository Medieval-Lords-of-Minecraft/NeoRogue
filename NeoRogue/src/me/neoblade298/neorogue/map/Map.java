package me.neoblade298.neorogue.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neorogue.area.AreaType;

public class Map {
	private static HashMap<AreaType, LinkedList<MapPiece>> allPieces = new HashMap<AreaType, LinkedList<MapPiece>>();
	private static HashMap<AreaType, Integer> piecesUsed = new HashMap<AreaType, Integer>();
	
	private ArrayList<MapEntrance> entrances = new ArrayList<MapEntrance>();
	private ArrayList<MapPiece> pieces = new ArrayList<MapPiece>();
	private boolean[][] shape = new boolean[8][8];
	private int size;
	
	public static Map generate(AreaType type, int size) {
		Map map = new Map();
		LinkedList<MapPiece> pieces = allPieces.get(type);
		for (int i = 0; i < size; i++) {
			MapPiece piece = pieces.poll();
			while (map.entrances.size() < 2 && piece.getNumEntrances() < 2 && i < size - 1) { // Make sure there are enough entrances to expand
				pieces.addLast(piece);
				piece = pieces.poll();
			}
		}
		
		int used = piecesUsed.getOrDefault(type, 0) + size;
		if (used > 20) {
			used = 0;
			Collections.shuffle(pieces);
		}
		piecesUsed.put(type, used);
		
		return map;
	}
	
	public void place(MapPiece piece) {
		MapShape shape = piece.getShape();
		// Special case, first piece being placed
		if (entrances.size() == 0) {
			int x = 4 - (shape.getHeight() / 2);
			int y = 4 - (shape.getLength() / 2);
			
		}
	}
}
