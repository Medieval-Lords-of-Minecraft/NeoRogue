package me.neoblade298.neorogue.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.bukkit.Bukkit;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.region.Region;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.fight.FightInstance;

public class FrozenWastesMap extends Map {
	private static final double DIRECT_CONNECT_CHANCE = 0.4;
	private List<MapEntrance[]> pathConnectedEntrances = new ArrayList<>();
	private HashMap<String, Integer> chunkHeights = new HashMap<>();

	public FrozenWastesMap(RegionType type) {
		super(type);
	}

	/**
	 * Generate piece placement for FROZEN_WASTES.
	 * Places 2-4 pieces using entrance-based connections. Pieces either connect
	 * directly (flush, same Y) or via mountain path (1-stride gap, random Y offset).
	 * Respects piece shapes for collision checking.
	 */
	public void generatePieces(RegionType type, boolean debugMode) {
		RegionType lookupType = debugMode ? RegionType.getDebugRegion(type) : type;
		LinkedList<MapPiece> avail = getStandardPieces(lookupType);
		LinkedList<MapPiece> used = getUsedPieces(lookupType);

		int targetCount = 2 + NeoRogue.gen.nextInt(3); // 2, 3, or 4

		// Place first piece centered
		MapPiece firstPiece = pollPiece(avail, used, lookupType);
		if (firstPiece == null) return;
		placeFirst(firstPiece, true);
		// Record height for first piece's chunks (Y=0)
		recordChunkHeights(getPieces().get(0), 0);
		MapPieceInstance firstInst = getPieces().get(0);
		Bukkit.getLogger().info("[FrozenWastes] Piece 0: " + firstPiece.getId()
				+ " at (" + firstInst.getX() + "," + firstInst.getZ() + ") Y=0 [direct/first]");

		// Place subsequent pieces
		int totalAttempts = avail.size() + used.size();
		for (int i = 1; i < targetCount; i++) {
			boolean placed = false;
			int attempts = 0;

			while (!placed && attempts < totalAttempts) {
				attempts++;
				MapPiece piece = pollPiece(avail, used, lookupType);
				if (piece == null) break;
				if (piece.getEntrances() == null || piece.getEntrances().length == 0) continue;

				boolean tryDirect = NeoRogue.gen.nextDouble() < DIRECT_CONNECT_CHANCE;

				if (tryDirect) {
					placed = tryDirectConnect(piece);
					if (!placed) placed = tryPathConnect(piece);
				} else {
					placed = tryPathConnect(piece);
					if (!placed) placed = tryDirectConnect(piece);
				}
			}

			if (!placed) {
				Bukkit.getLogger().warning("[NeoRogue] FrozenWastes: Could not place piece " + i + ". Stopping.");
				break;
			}
		}

		// Scan unused entrances for additional path connections
		scanUnusedEntrancesForPaths();

		Bukkit.getLogger().info("[FrozenWastes] Placed " + getPieces().size() + " pieces, "
				+ pathConnectedEntrances.size() + " path connections, "
				+ entrances.size() + " unused entrances");
	}

	/**
	 * Try to place a piece directly connected to an existing entrance (flush, same Y).
	 * Uses the same entrance-matching logic as Map.place().
	 */
	private boolean tryDirectConnect(MapPiece piece) {
		TreeSet<MapPieceInstance> potentialPlacements = new TreeSet<>();
		for (MapEntrance available : entrances) {
			for (MapEntrance potential : piece.getEntrances()) {
				if (!available.tagsCompatible(potential)) continue;
				for (MapPieceInstance pSettings : piece.getRotationOptions(available, potential)) {
					piece.getShape().applySettings(pSettings);
					int[] offset = pSettings.calculateOffset(available);
					if (canPlace(piece.getShape(), offset[0], offset[2])) {
						pSettings.setX(offset[0]);
						pSettings.setY(offset[1]);
						pSettings.setZ(offset[2]);
						potentialPlacements.add(pSettings);
					}
				}
			}
			if (potentialPlacements.size() > 20) break;
		}
		if (potentialPlacements.isEmpty()) return false;

		// Pick a random placement from the top candidates
		MapPieceInstance[] arr = potentialPlacements.toArray(new MapPieceInstance[0]);
		MapPieceInstance chosen = arr[NeoRogue.gen.nextInt(Math.min(arr.length, 5))];

		// Combine the neighbor's base Y with the entrance Y-delta already stored in chosen.getY()
		int neighborY = getNeighborY(chosen);
		int finalY = neighborY + chosen.getY();
		chosen.setY(finalY);

		placePiece(chosen, false);
		recordChunkHeights(chosen, finalY);
		Bukkit.getLogger().info("[FrozenWastes] Piece: " + piece.getId()
				+ " at (" + chosen.getX() + "," + chosen.getZ() + ") Y=" + finalY + " [direct]");
		return true;
	}

	/**
	 * Try to place a piece with a 1-stride gap from an existing entrance,
	 * connected via mountain path. Assigns a random Y offset.
	 */
	private boolean tryPathConnect(MapPiece piece) {
		// Shuffle entrances for variety
		ArrayList<MapEntrance> shuffledEntrances = new ArrayList<>(entrances);
		Collections.shuffle(shuffledEntrances, NeoRogue.gen);

		for (MapEntrance available : shuffledEntrances) {
			// The gap position is 1 stride unit in the direction the entrance faces
			int gapX = (int) available.getXFacing();
			int gapZ = (int) available.getZFacing();

			// The piece needs to be placed such that one of its entrances faces back into the gap
			Direction neededDir = available.getDirection().invert();

			for (MapEntrance potential : piece.getEntrances()) {
				// Create instance rotated so potential entrance faces neededDir
				MapPieceInstance inst = piece.getInstance();
				// Calculate rotation: we want potential's direction (after rotation) to equal neededDir
				int rotAmount = (neededDir.getValue() - potential.getOriginalDirection().getValue() + 4) % 4;
				inst.setRotations(rotAmount);
				piece.getShape().applySettings(inst);

				// The piece entrance after rotation
				MapEntrance rotatedEntrance = potential.clone().applySettings(inst);

				// Calculate piece position so that its entrance's facing chunk is gapX/gapZ
				// (the gap between the two pieces is exactly 1 grid cell wide)
				int pieceX, pieceZ;
				switch (neededDir) {
				case NORTH: // Piece faces north: facing = entranceZ+1. Want facing=gapZ → entrance=gapZ-1
					pieceX = gapX - (int) rotatedEntrance.getX();
					pieceZ = gapZ - 1 - (int) rotatedEntrance.getZ();
					break;
				case SOUTH: // Piece faces south: facing = entranceZ-1. Want facing=gapZ → entrance=gapZ+1
					pieceX = gapX - (int) rotatedEntrance.getX();
					pieceZ = gapZ + 1 - (int) rotatedEntrance.getZ();
					break;
				case EAST: // Piece faces east: facing = entranceX+1. Want facing=gapX → entrance=gapX-1
					pieceX = gapX - 1 - (int) rotatedEntrance.getX();
					pieceZ = gapZ - (int) rotatedEntrance.getZ();
					break;
				case WEST: // Piece faces west: facing = entranceX-1. Want facing=gapX → entrance=gapX+1
					pieceX = gapX + 1 - (int) rotatedEntrance.getX();
					pieceZ = gapZ - (int) rotatedEntrance.getZ();
					break;
				default:
					continue;
				}

				if (!canPlace(piece.getShape(), pieceX, pieceZ)) continue;

				// Also try flipped variant
				MapPieceInstance chosen = inst;
				chosen.setX(pieceX);
				chosen.setZ(pieceZ);

				int yOffset = NeoRogue.gen.nextInt(5) - 2;
				chosen.setY(yOffset);

				placePiece(chosen, false);
				recordChunkHeights(chosen, yOffset);

				// Record the path connection between the available entrance and the piece's entrance
				MapEntrance placedEntrance = potential.clone().applySettings(chosen);
				pathConnectedEntrances.add(new MapEntrance[] { available, placedEntrance });
				Bukkit.getLogger().info("[FrozenWastes] Piece: " + piece.getId()
						+ " at (" + pieceX + "," + pieceZ + ") Y=" + yOffset + " [path]");
				Bukkit.getLogger().info("[FrozenWastes]   Path: ("
						+ (int) available.getX() + "," + (int) available.getZ() + " " + available.getDirection()
						+ ") -> (" + (int) placedEntrance.getX() + "," + (int) placedEntrance.getZ()
						+ " " + placedEntrance.getDirection() + ")");

				// Remove the original available entrance since it's now path-connected
				entrances.remove(available);
				// Remove the new piece's matching entrance from available list
				entrances.removeIf(e -> e.getX() == placedEntrance.getX()
						&& e.getZ() == placedEntrance.getZ()
						&& e.getDirection() == placedEntrance.getDirection());
				return true;
			}
		}
		return false;
	}

	/**
	 * After all pieces are placed, scan unused entrances for pairs that face each other
	 * across a 1-stride gap and add them as path connections.
	 */
	private void scanUnusedEntrancesForPaths() {
		ArrayList<MapEntrance> remaining = new ArrayList<>(entrances);
		ArrayList<MapEntrance> used = new ArrayList<>();

		for (int i = 0; i < remaining.size(); i++) {
			MapEntrance a = remaining.get(i);
			if (used.contains(a)) continue;

			for (int j = i + 1; j < remaining.size(); j++) {
				MapEntrance b = remaining.get(j);
				if (used.contains(b)) continue;

				// Check if they face each other across a gap
				if (a.getDirection().equals(b.getDirection().invert())
						&& a.getXFacing() == b.getX() + (b.getDirection() == Direction.EAST ? 1 : b.getDirection() == Direction.WEST ? -1 : 0)
						&& a.getZFacing() == b.getZ() + (b.getDirection() == Direction.NORTH ? 1 : b.getDirection() == Direction.SOUTH ? -1 : 0)) {
					// They're 1 apart and facing each other — connect with path
					// Actually simpler check: entrance A's facing position is adjacent to entrance B's position
				}

				// Simpler: check Manhattan distance of facing positions
				double dx = Math.abs(a.getXFacing() - b.getXFacing());
				double dz = Math.abs(a.getZFacing() - b.getZFacing());
				boolean oppositeDirs = a.getDirection().equals(b.getDirection().invert());

				if (oppositeDirs && ((dx <= 1 && dz == 0) || (dx == 0 && dz <= 1))) {
					pathConnectedEntrances.add(new MapEntrance[] { a, b });
					Bukkit.getLogger().info("[FrozenWastes]   Scan path: ("
							+ (int) a.getX() + "," + (int) a.getZ() + " " + a.getDirection()
							+ ") -> (" + (int) b.getX() + "," + (int) b.getZ() + " " + b.getDirection() + ")");
					used.add(a);
					used.add(b);
					break;
				}
			}
		}
	}

	/**
	 * Get the Y offset of the piece that owns the entrance the new piece connects to.
	 */
	private int getNeighborY(MapPieceInstance inst) {
		// The instance was created from an available entrance, look at the piece at that position
		MapEntrance entrance = inst.getEntrance();
		if (entrance != null) {
			// Find which piece the connecting entrance belongs to by checking chunk heights
			int ex = (int) entrance.getX();
			int ez = (int) entrance.getZ();
			Integer height = chunkHeights.get(ex + "," + ez);
			if (height != null) return height;
		}
		return 0;
	}

	private void recordChunkHeights(MapPieceInstance inst, int yOffset) {
		MapShape shape = inst.getPiece().getShape();
		shape.applySettings(inst);
		for (int i = 0; i < shape.getLength(); i++) {
			for (int j = 0; j < shape.getHeight(); j++) {
				if (shape.get(i, j)) {
					chunkHeights.put((inst.getX() + i) + "," + (inst.getZ() + j), yOffset);
				}
			}
		}
	}

	private MapPiece pollPiece(LinkedList<MapPiece> avail, LinkedList<MapPiece> used, RegionType type) {
		if (avail.isEmpty()) {
			Collections.shuffle(used);
			avail.addAll(used);
		}
		MapPiece piece = avail.poll();
		if (piece != null) {
			used.add(piece);
			addTargets(piece.getTargets());
		}
		return piece;
	}

	@Override
	protected void generateTerrain(FightInstance fi, int xOff, int zOff) {
		MountainPathGenerator.generateFrozenWastes(
			Bukkit.getWorld(Region.getActiveWorldName()),
			xOff, zOff, getMapSize(),
			getShape(), pathConnectedEntrances, chunkHeights,
			NeoRogue.gen.nextLong()
		);
	}

	@Override
	protected boolean shouldBlockEntrances() {
		return false;
	}

	@Override
	protected void postDeserialize() {
		for (MapPieceInstance inst : getPieces()) {
			recordChunkHeights(inst, inst.getY());
		}
		scanUnusedEntrancesForPaths();
	}
}
