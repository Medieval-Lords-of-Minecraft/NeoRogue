package me.neoblade298.neorogue.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.region.Region;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.fight.FightInstance;

public class FrozenWastesMap extends Map {
	private List<int[]> adjacentChunkPairs = new ArrayList<>();
	private HashMap<String, Integer> chunkHeights = new HashMap<>();

	public FrozenWastesMap(RegionType type) {
		super(type);
		setWorldStride(2);
	}

	/**
	 * Generate piece placement for FROZEN_WASTES.
	 * Picks 3-5 isolated chunk positions clustered near the center of the grid,
	 * assigns each a random Y height offset, then places one map piece per position.
	 * Adjacent chunk pairs (1 apart in the logical grid) are recorded so that
	 * MountainPathGenerator can carve corridor paths between them.
	 */
	public void generatePieces(RegionType type, boolean debugMode) {
		RegionType lookupType = debugMode ? RegionType.getDebugRegion(type) : type;
		LinkedList<MapPiece> avail = getStandardPieces(lookupType);
		LinkedList<MapPiece> used = getUsedPieces(lookupType);

		int mapSize = getMapSize();
		int targetCount = 3 + NeoRogue.gen.nextInt(3); // 3, 4, or 5
		int center = mapSize / 2;
		int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};

		ArrayList<int[]> selected = new ArrayList<>();
		selected.add(new int[]{center, center});

		int maxAttempts = 200;
		int attempts = 0;
		while (selected.size() < targetCount && attempts < maxAttempts) {
			attempts++;
			int[] base = selected.get(NeoRogue.gen.nextInt(selected.size()));
			int[] dir = dirs[NeoRogue.gen.nextInt(4)];
			int nx = base[0] + dir[0];
			int nz = base[1] + dir[1];
			if (nx < 1 || nx >= mapSize - 1 || nz < 1 || nz >= mapSize - 1) continue;

			boolean conflict = false;
			for (int[] pos : selected) {
				if (pos[0] == nx && pos[1] == nz) { conflict = true; break; }
			}
			if (!conflict) selected.add(new int[]{nx, nz});
		}

		// Place one piece at each selected position
		for (int[] pos : selected) {
			if (avail.isEmpty()) {
				Collections.shuffle(used);
				avail.addAll(used);
			}
			MapPiece piece = avail.poll();
			if (piece == null) {
				Bukkit.getLogger().warning("[NeoRogue] No pieces available for " + lookupType + " map generation. Stopping placement.");
				break;
			}
			used.add(piece);
			addTargets(piece.getTargets());

			MapPieceInstance inst = piece.getInstance();
			inst.setRotations(NeoRogue.gen.nextInt(4));
			int flipRand = NeoRogue.gen.nextInt(3);
			if (flipRand == 1) inst.setFlip(true, false);
			else if (flipRand == 2) inst.setFlip(false, true);
			piece.getShape().applySettings(inst);

			int yOffset = NeoRogue.gen.nextInt(9) - 4;
			inst.setX(pos[0]);
			inst.setY(yOffset);
			inst.setZ(pos[1]);

			chunkHeights.put(pos[0] + "," + pos[1], yOffset);
			placePiece(inst, false);
		}

		// Find adjacent pairs (exactly 1 apart in one axis) for path generation
		for (int i = 0; i < selected.size(); i++) {
			for (int j = i + 1; j < selected.size(); j++) {
				int[] a = selected.get(i);
				int[] b = selected.get(j);
				int dx = Math.abs(a[0] - b[0]);
				int dz = Math.abs(a[1] - b[1]);
				if ((dx == 1 && dz == 0) || (dx == 0 && dz == 1)) {
					adjacentChunkPairs.add(new int[]{a[0], a[1], b[0], b[1]});
				}
			}
		}
	}

	@Override
	protected void generateTerrain(FightInstance fi, int xOff, int zOff) {
		MountainPathGenerator.generateFrozenWastes(
			Bukkit.getWorld(Region.WORLD_NAME),
			xOff, zOff, getMapSize(), getWorldStride(),
			getShape(), adjacentChunkPairs, chunkHeights,
			NeoRogue.gen.nextLong()
		);
	}

	@Override
	protected boolean shouldBlockEntrances() {
		return false;
	}
}
