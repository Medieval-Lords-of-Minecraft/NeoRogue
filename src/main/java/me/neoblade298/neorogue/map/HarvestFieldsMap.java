package me.neoblade298.neorogue.map;

import java.util.HashSet;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.region.RegionType;

public class HarvestFieldsMap extends Map {

	public HarvestFieldsMap(RegionType type) {
		super(type);
	}

	@Override
	protected boolean canPlaceOnEdge() {
		return false;
	}

	@Override
	protected void postGenerate(RegionType type) {
		boolean hasEntrances = getPieces().getFirst().getPiece().getEntrances() != null;
		int y = hasEntrances ? (int) getPieces().getFirst().getPiece().getEntrances()[0].getY() : 0;
		boolean[][] shape = getShape();
		HashSet<int[]> set = new HashSet<>();
		// Look for all empty spots adjacent to used spots
		for (int i = 0; i < shape.length; i++) {
			for (int j = 0; j < shape[0].length; j++) {
				if (!shape[i][j]) continue;
				for (int x = -1; x <= 1; x++) {
					for (int z = -1; z <= 1; z++) {
						int ip = x + i, jp = z + j;
						if (ip < 0 || ip >= shape.length) continue;
						if (jp < 0 || jp >= shape.length) continue;
						if (shape[ip][jp]) continue;
						set.add(new int[]{ip, jp});
					}
				}
			}
		}

		// Only have harvest fields borders for standard maps
		if (getPieces().getFirst().getType().equals("STANDARD")) {
			for (int[] coords : set) {
				MapPieceInstance inst = MapPiece.HARVESTBORDER.getInstance();
				MapShape ms = inst.getPiece().getShape();

				// Randomly rotate the piece
				inst.setRotations(NeoRogue.gen.nextInt(4));
				int rand = NeoRogue.gen.nextInt(3);
				if (rand == 1) inst.setFlip(true, false);
				else if (rand == 2) inst.setFlip(false, true);
				ms.applySettings(inst);
				inst.setX(coords[0]);
				inst.setY(y);
				inst.setZ(coords[1]);
				placePiece(inst, false);
			}
		}
	}

	@Override
	protected boolean shouldBlockEntrances() {
		return false;
	}
}
