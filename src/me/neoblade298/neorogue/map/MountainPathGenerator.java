package me.neoblade298.neorogue.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Generates procedural mountain terrain with carved paths between connected map pieces.
 * Used for FROZEN_WASTES region where pieces are spaced apart with mountain terrain filling gaps.
 */
public class MountainPathGenerator {
	private static final int CORRIDOR_HALF_WIDTH = 3;
	private static final int TRANSITION_WIDTH = 6;
	private static final int MIN_MOUNTAIN_HEIGHT = 6;
	private static final int MAX_MOUNTAIN_HEIGHT = 22;
	private static final double NOISE_SCALE = 0.02;
	private static final int FOUNDATION_DEPTH = 3;
	// How many blocks of padding around the piece bounding box to generate mountains
	private static final int PADDING_CHUNKS = 2;

	/**
	 * Generate mountain terrain and path corridors for a spaced map.
	 * Works in "terrain space" where coordinates are positive, then converts to world space.
	 * 
	 * @param world          Target world
	 * @param xOff           Plot X offset
	 * @param zOff           Plot Z offset
	 * @param mapSize        Logical grid size (e.g. 12)
	 * @param worldStride    Stride multiplier (e.g. 2 = 1 chunk gap between pieces)
	 * @param logicalShape   logicalShape[x][z] = true if that logical chunk is occupied by a piece
	 * @param connectedPairs Connected entrance pairs for path generation
	 * @param seed           Noise seed
	 */
	public static void generate(World world, int xOff, int zOff,
			int mapSize, int worldStride,
			boolean[][] logicalShape,
			List<MapEntrance[]> connectedPairs,
			long seed) {

		long startTime = System.currentTimeMillis();
		SimplexNoise noise = new SimplexNoise(seed);
		int baseY = MapPieceInstance.Y_OFFSET;

		// 1. Determine active bounding box in logical space
		int minGX = mapSize, maxGX = 0, minGZ = mapSize, maxGZ = 0;
		for (int gx = 0; gx < mapSize; gx++) {
			for (int gz = 0; gz < mapSize; gz++) {
				if (!logicalShape[gx][gz]) continue;
				minGX = Math.min(minGX, gx);
				maxGX = Math.max(maxGX, gx);
				minGZ = Math.min(minGZ, gz);
				maxGZ = Math.max(maxGZ, gz);
			}
		}
		if (minGX > maxGX) return; // No pieces placed

		// Convert to terrain-space block bounds with padding
		int startTX = Math.max(0, (minGX - PADDING_CHUNKS) * worldStride * 16);
		int endTX = Math.min(mapSize * worldStride * 16, (maxGX + 1 + PADDING_CHUNKS) * worldStride * 16);
		int startTZ = Math.max(0, (minGZ - PADDING_CHUNKS) * worldStride * 16);
		int endTZ = Math.min(mapSize * worldStride * 16, (maxGZ + 1 + PADDING_CHUNKS) * worldStride * 16);
		int width = endTX - startTX;
		int depth = endTZ - startTZ;

		// 2. Build protection mask (piece footprints in terrain space)
		boolean[][] protectedMask = new boolean[width][depth];
		for (int gx = 0; gx < mapSize; gx++) {
			for (int gz = 0; gz < mapSize; gz++) {
				if (!logicalShape[gx][gz]) continue;
				int csx = gx * worldStride * 16 - startTX;
				int csz = gz * worldStride * 16 - startTZ;
				for (int dx = 0; dx < 16; dx++) {
					for (int dz = 0; dz < 16; dz++) {
						int px = csx + dx;
						int pz = csz + dz;
						if (px >= 0 && px < width && pz >= 0 && pz < depth) {
							protectedMask[px][pz] = true;
						}
					}
				}
			}
		}

		// 3. Compute path segments (each is [x1,z1,x2,z2] in terrain space)
		List<int[]> pathSegments = computePathSegments(connectedPairs, worldStride, startTX, startTZ);

		// 4. Pre-compute distance-to-nearest-path for every terrain block
		// Use squared distance to avoid sqrt in inner loop, compute actual distance only when needed
		double[][] pathDist = new double[width][depth];
		for (double[] row : pathDist) Arrays.fill(row, Double.MAX_VALUE);

		for (int[] seg : pathSegments) {
			int sx1 = seg[0], sz1 = seg[1], sx2 = seg[2], sz2 = seg[3];
			// Determine bounding box of influence around this segment
			int influence = CORRIDOR_HALF_WIDTH + TRANSITION_WIDTH + 5;
			int bMinX = Math.max(0, Math.min(sx1, sx2) - influence);
			int bMaxX = Math.min(width - 1, Math.max(sx1, sx2) + influence);
			int bMinZ = Math.max(0, Math.min(sz1, sz2) - influence);
			int bMaxZ = Math.min(depth - 1, Math.max(sz1, sz2) + influence);

			for (int tx = bMinX; tx <= bMaxX; tx++) {
				for (int tz = bMinZ; tz <= bMaxZ; tz++) {
					double dist = distToSegment(tx, tz, sx1, sz1, sx2, sz2);
					if (dist < pathDist[tx][tz]) {
						pathDist[tx][tz] = dist;
					}
				}
			}
		}

		// 5. Generate terrain
		int blocksPlaced = 0;
		for (int lx = 0; lx < width; lx++) {
			for (int lz = 0; lz < depth; lz++) {
				if (protectedMask[lx][lz]) continue;

				int tx = startTX + lx;
				int tz = startTZ + lz;
				// Convert terrain space → world space
				int worldX = -(tx + xOff + MapPieceInstance.X_FIGHT_OFFSET);
				int worldZ = tz + zOff + MapPieceInstance.Z_FIGHT_OFFSET;

				double dist = pathDist[lx][lz];
				double elevation = computeElevation(noise, tx, tz, dist);

				blocksPlaced += placeColumn(world, worldX, worldZ, baseY, noise, elevation, dist, tx, tz);
			}
		}

		long duration = System.currentTimeMillis() - startTime;
		Bukkit.getLogger().info("[MountainPath] Generated " + blocksPlaced + " blocks in " + duration + "ms"
				+ " (" + width + "x" + depth + " area, " + pathSegments.size() + " paths)");
	}

	/**
	 * Compute path anchor points from connected entrance pairs.
	 * Each entrance's direction (stored inverted) tells us which edge of the piece the doorway is on.
	 * The path anchor is at the edge of the piece's world-space chunk.
	 */
	private static List<int[]> computePathSegments(List<MapEntrance[]> pairs, int worldStride,
			int startTX, int startTZ) {
		List<int[]> segments = new ArrayList<>();
		for (MapEntrance[] pair : pairs) {
			int[] a1 = getPathAnchor(pair[0], worldStride);
			int[] a2 = getPathAnchor(pair[1], worldStride);
			// Convert to local coordinates relative to our generation area
			segments.add(new int[] {
					a1[0] - startTX, a1[1] - startTZ,
					a2[0] - startTX, a2[1] - startTZ
			});
		}
		return segments;
	}

	/**
	 * Get the terrain-space path anchor point for an entrance.
	 * The anchor is at the edge of the entrance's chunk, centered on the perpendicular axis.
	 */
	private static int[] getPathAnchor(MapEntrance entrance, int worldStride) {
		int cx = (int) entrance.getX() * worldStride * 16;
		int cz = (int) entrance.getZ() * worldStride * 16;
		int anchorX, anchorZ;

		// The stored direction is inverted from the YAML direction.
		// WEST stored = entrance on west wall = path approaches from west
		switch (entrance.getDirection()) {
		case WEST:
			anchorX = cx - 1;
			anchorZ = cz + 8;
			break;
		case EAST:
			anchorX = cx + 16;
			anchorZ = cz + 8;
			break;
		case SOUTH:
			anchorX = cx + 8;
			anchorZ = cz - 1;
			break;
		case NORTH:
			anchorX = cx + 8;
			anchorZ = cz + 16;
			break;
		default:
			anchorX = cx + 8;
			anchorZ = cz + 8;
			break;
		}
		return new int[] { anchorX, anchorZ };
	}

	/**
	 * Compute the minimum distance from point (px,pz) to a line segment (x1,z1)-(x2,z2).
	 */
	private static double distToSegment(double px, double pz, double x1, double z1, double x2, double z2) {
		double dx = x2 - x1;
		double dz = z2 - z1;
		double lenSq = dx * dx + dz * dz;
		if (lenSq == 0) return Math.sqrt((px - x1) * (px - x1) + (pz - z1) * (pz - z1));

		double t = Math.max(0, Math.min(1, ((px - x1) * dx + (pz - z1) * dz) / lenSq));
		double projX = x1 + t * dx;
		double projZ = z1 + t * dz;
		return Math.sqrt((px - projX) * (px - projX) + (pz - projZ) * (pz - projZ));
	}

	/**
	 * Compute terrain elevation at a given terrain-space position.
	 * Returns height in blocks above baseY.
	 * - Path zone (dist <= CORRIDOR_HALF_WIDTH): 0 (flat)
	 * - Transition zone: gradual rise
	 * - Mountain zone: noise-based elevation
	 */
	private static double computeElevation(SimplexNoise noise, int tx, int tz, double distToPath) {
		// Base noise elevation (multi-octave)
		double e = 0;
		e += noise.noise(tx * NOISE_SCALE, tz * NOISE_SCALE) * 0.5;
		e += noise.noise(tx * NOISE_SCALE * 2, tz * NOISE_SCALE * 2) * 0.25;
		e += noise.noise(tx * NOISE_SCALE * 4, tz * NOISE_SCALE * 4) * 0.125;
		e += noise.noise(tx * NOISE_SCALE * 8, tz * NOISE_SCALE * 8) * 0.0625;
		// Normalize to 0-1
		e = (e + 1) / 2;
		e = Math.pow(e, 1.2);
		e = Math.max(0, Math.min(1, e));

		double mountainHeight = MIN_MOUNTAIN_HEIGHT + e * (MAX_MOUNTAIN_HEIGHT - MIN_MOUNTAIN_HEIGHT);

		if (distToPath <= CORRIDOR_HALF_WIDTH) {
			// Flat path
			return 0;
		} else if (distToPath <= CORRIDOR_HALF_WIDTH + TRANSITION_WIDTH) {
			// Transition: smooth rise from path to mountain
			double t = (distToPath - CORRIDOR_HALF_WIDTH) / TRANSITION_WIDTH;
			// Smooth step for natural transition
			t = t * t * (3 - 2 * t);
			return mountainHeight * t;
		} else {
			// Full mountain
			return mountainHeight;
		}
	}

	/**
	 * Place a vertical column of terrain blocks at the given world position.
	 * Returns number of blocks placed.
	 */
	private static int placeColumn(World world, int worldX, int worldZ, int baseY,
			SimplexNoise noise, double elevation, double distToPath,
			int tx, int tz) {
		int surfaceY = baseY + (int) elevation;
		int placed = 0;

		// Foundation below baseY
		for (int y = baseY - FOUNDATION_DEPTH; y < baseY; y++) {
			Block b = world.getBlockAt(worldX, y, worldZ);
			b.setType(Material.STONE, false);
			placed++;
		}

		if (elevation < 0.5) {
			// Path zone: flat surface
			Block surface = world.getBlockAt(worldX, baseY, worldZ);
			surface.setType(Material.PACKED_ICE, false);
			placed++;
			// Railing/wall if right at edge of corridor
			if (distToPath > CORRIDOR_HALF_WIDTH - 0.5 && distToPath <= CORRIDOR_HALF_WIDTH) {
				Block wall = world.getBlockAt(worldX, baseY + 1, worldZ);
				wall.setType(Material.SNOW_BLOCK, false);
				placed++;
			}
		} else {
			// Mountain/transition column
			double stoneVar = noise.noise(tx * 0.1, tz * 0.1);
			for (int y = baseY; y <= surfaceY; y++) {
				Block b = world.getBlockAt(worldX, y, worldZ);
				if (y == surfaceY) {
					// Surface layer
					b.setType(Material.SNOW_BLOCK, false);
				} else if (y > surfaceY - 3) {
					// Near surface
					if (elevation > 15) {
						b.setType(Material.PACKED_ICE, false);
					} else {
						b.setType(Material.STONE, false);
					}
				} else {
					// Core
					if (stoneVar > 0.3) {
						b.setType(Material.STONE, false);
					} else if (stoneVar > 0) {
						b.setType(Material.ANDESITE, false);
					} else {
						b.setType(Material.COBBLESTONE, false);
					}
				}
				placed++;
			}
		}

		return placed;
	}
}
