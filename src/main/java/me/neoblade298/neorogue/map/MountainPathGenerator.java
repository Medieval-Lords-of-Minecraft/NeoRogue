package me.neoblade298.neorogue.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;

import me.neoblade298.neorogue.region.Region;

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
	private static final int BARRIER_ROOF_Y = 90; // Fixed Y-level for barrier roof
	// How many blocks of padding around the piece bounding box to generate mountains
	private static final int PADDING_CHUNKS = 2;

	// Pre-adapted block states for WorldEdit bulk placement
	private static final BaseBlock WE_STONE = BukkitAdapter.adapt(Material.STONE.createBlockData()).toBaseBlock();
	private static final BaseBlock WE_ANDESITE = BukkitAdapter.adapt(Material.ANDESITE.createBlockData()).toBaseBlock();
	private static final BaseBlock WE_COBBLESTONE = BukkitAdapter.adapt(Material.COBBLESTONE.createBlockData()).toBaseBlock();
	private static final BaseBlock WE_SNOW_BLOCK = BukkitAdapter.adapt(Material.SNOW_BLOCK.createBlockData()).toBaseBlock();
	private static final BaseBlock WE_PACKED_ICE = BukkitAdapter.adapt(Material.PACKED_ICE.createBlockData()).toBaseBlock();
	private static final BaseBlock WE_BARRIER = BukkitAdapter.adapt(Material.BARRIER.createBlockData()).toBaseBlock();

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

		// 5. Generate terrain via WorldEdit EditSession for bulk placement
		int blocksPlaced = 0;
		try (EditSession editSession = WorldEdit.getInstance().newEditSession(Region.world)) {
			for (int lx = 0; lx < width; lx++) {
				for (int lz = 0; lz < depth; lz++) {
					if (protectedMask[lx][lz]) continue;

					int tx = startTX + lx;
					int tz = startTZ + lz;
					int worldX = -(tx + xOff + MapPieceInstance.X_FIGHT_OFFSET);
					int worldZ = tz + zOff + MapPieceInstance.Z_FIGHT_OFFSET;

					double dist = pathDist[lx][lz];
					double elevation = computeElevation(noise, tx, tz, dist);

					blocksPlaced += placeColumn(editSession, worldX, worldZ, baseY, noise, elevation, dist, tx, tz);
				}
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
	 * Returns the clamped projection parameter t (0 to 1) of point (px,pz) onto segment (x1,z1)-(x2,z2).
	 * t=0 means closest to endpoint A, t=1 means closest to endpoint B.
	 */
	private static double projectOntoSegment(double px, double pz, double x1, double z1, double x2, double z2) {
		double dx = x2 - x1;
		double dz = z2 - z1;
		double lenSq = dx * dx + dz * dz;
		if (lenSq == 0) return 0;
		return Math.max(0, Math.min(1, ((px - x1) * dx + (pz - z1) * dz) / lenSq));
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
			t = t * t * (3 - 2 * t);
			return mountainHeight * t;
		} else {
			// Full mountain
			return mountainHeight;
		}
	}

	/**
	 * Generate terrain for the FROZEN_WASTES region using entrance-based path connections.
	 * Computes path anchors from entrance directions, then delegates to the main implementation.
	 */
	public static void generateFrozenWastes(World world, int xOff, int zOff,
			int mapSize, int worldStride,
			boolean[][] logicalShape,
			List<MapEntrance[]> entrancePairs,
			HashMap<String, Integer> chunkHeights,
			long seed) {

		// Compute path segments directly from entrance anchors with Y offsets
		// We need startTX/startTZ to convert to local coords, so compute bounding box first
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
		if (minGX > maxGX) return;
		int startTX = Math.max(0, (minGX - PADDING_CHUNKS) * worldStride * 16);
		int startTZ = Math.max(0, (minGZ - PADDING_CHUNKS) * worldStride * 16);

		List<int[]> pathSegments = new ArrayList<>();
		for (MapEntrance[] pair : entrancePairs) {
			int[] a1 = getPathAnchor(pair[0], worldStride);
			int[] a2 = getPathAnchor(pair[1], worldStride);
			// Use entrance's full Y (local height within piece + piece placement offset)
			int yA = (int) pair[0].getY();
			int yB = (int) pair[1].getY();
			pathSegments.add(new int[] {
				a1[0] - startTX, a1[1] - startTZ,
				a2[0] - startTX, a2[1] - startTZ,
				yA, yB
			});
		}

		generateFrozenWastesWithSegments(world, xOff, zOff, mapSize, worldStride,
				logicalShape, pathSegments, chunkHeights, seed);
	}

	/**
	 * Generate terrain for the FROZEN_WASTES region.
	 * Flat 16x16 platforms are placed at each piece chunk at the specified height offsets.
	 * Paths are carved between pre-computed path segments.
	 * Mountainous terrain fills the remainder.
	 *
	 * @param world              Target world
	 * @param xOff               Plot X offset
	 * @param zOff               Plot Z offset
	 * @param mapSize            Logical grid size (e.g. 12)
	 * @param worldStride        Stride multiplier (e.g. 2 = 1 chunk gap between pieces)
	 * @param logicalShape       logicalShape[x][z] = true if that logical chunk has a piece
	 * @param pathSegments       Pre-computed path segments: int[]{sx1, sz1, sx2, sz2, yOffA, yOffB}
	 * @param chunkHeights       Map from "x,z" logical chunk key to Y offset above baseY
	 * @param seed               Noise seed
	 */
	private static void generateFrozenWastesWithSegments(World world, int xOff, int zOff,
			int mapSize, int worldStride,
			boolean[][] logicalShape,
			List<int[]> pathSegments,
			HashMap<String, Integer> chunkHeights,
			long seed) {

		long startTime = System.currentTimeMillis();
		long phaseStart = startTime;
		SimplexNoise noise = new SimplexNoise(seed);
		int baseY = MapPieceInstance.Y_OFFSET;
		int terrainRadius = 16; // How far mountains generate from pieces/paths
		int pieceBarrierRadius = 3; // Tight barrier wall around pieces
		int pathBarrierRadius = CORRIDOR_HALF_WIDTH + TRANSITION_WIDTH; // Wider barrier around paths

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
		if (minGX > maxGX) return;

		// Convert to terrain-space block bounds with padding
		int startTX = Math.max(0, (minGX - PADDING_CHUNKS) * worldStride * 16);
		int endTX = Math.min(mapSize * worldStride * 16, (maxGX + 1 + PADDING_CHUNKS) * worldStride * 16);
		int startTZ = Math.max(0, (minGZ - PADDING_CHUNKS) * worldStride * 16);
		int endTZ = Math.min(mapSize * worldStride * 16, (maxGZ + 1 + PADDING_CHUNKS) * worldStride * 16);
		int width = endTX - startTX;
		int depth = endTZ - startTZ;

		// 2. Build per-column piece height map (Integer.MIN_VALUE = not a piece chunk)
		int[][] pieceHeightMap = new int[width][depth];
		for (int[] row : pieceHeightMap) Arrays.fill(row, Integer.MIN_VALUE);

		int cellSize = worldStride * 16; // Full stride-cell size in blocks
		for (int gx = 0; gx < mapSize; gx++) {
			for (int gz = 0; gz < mapSize; gz++) {
				if (!logicalShape[gx][gz]) continue;
				int yOff = chunkHeights.getOrDefault(gx + "," + gz, 0);
				int csx = gx * cellSize - startTX;
				int csz = gz * cellSize - startTZ;
				for (int dx = 0; dx < cellSize; dx++) {
					for (int dz = 0; dz < cellSize; dz++) {
						int px = csx + dx;
						int pz = csz + dz;
						if (px >= 0 && px < width && pz >= 0 && pz < depth) {
							pieceHeightMap[px][pz] = yOff;
						}
					}
				}
			}
		}

		// 3. Pre-compute distance to nearest piece or path for every terrain block
		double[][] nearestDist = new double[width][depth];
		double[][] pieceDist = new double[width][depth]; // Distance to nearest piece edge only
		double[][] pathDistArr = new double[width][depth]; // Distance to nearest path centerline only
		for (double[] row : nearestDist) Arrays.fill(row, Double.MAX_VALUE);
		for (double[] row : pieceDist) Arrays.fill(row, Double.MAX_VALUE);
		for (double[] row : pathDistArr) Arrays.fill(row, Double.MAX_VALUE);

		// Distance to piece footprints
		for (int lx = 0; lx < width; lx++) {
			for (int lz = 0; lz < depth; lz++) {
				if (pieceHeightMap[lx][lz] != Integer.MIN_VALUE) {
					nearestDist[lx][lz] = 0;
					pieceDist[lx][lz] = 0;
				}
			}
		}
		// Propagate piece distances outward within borderRadius using a simple BFS-like sweep
		// Also track the Y offset of the nearest piece block for each terrain block
		double[][] nearestPieceY = new double[width][depth];
		for (int lx = 0; lx < width; lx++) {
			for (int lz = 0; lz < depth; lz++) {
				if (pieceHeightMap[lx][lz] == Integer.MIN_VALUE) continue;
				int minX = Math.max(0, lx - terrainRadius);
				int maxX = Math.min(width - 1, lx + terrainRadius);
				int minZ = Math.max(0, lz - terrainRadius);
				int maxZ = Math.min(depth - 1, lz + terrainRadius);
				for (int nx = minX; nx <= maxX; nx++) {
					for (int nz = minZ; nz <= maxZ; nz++) {
						double d = Math.sqrt((nx - lx) * (nx - lx) + (nz - lz) * (nz - lz));
						if (d < nearestDist[nx][nz]) nearestDist[nx][nz] = d;
						if (d < pieceDist[nx][nz]) {
							pieceDist[nx][nz] = d;
							nearestPieceY[nx][nz] = pieceHeightMap[lx][lz];
						}
					}
				}
			}
		}

		// Distance to path segments
		for (int[] seg : pathSegments) {
			int sx1 = seg[0], sz1 = seg[1], sx2 = seg[2], sz2 = seg[3];
			int influence = CORRIDOR_HALF_WIDTH + terrainRadius;
			int bMinX = Math.max(0, Math.min(sx1, sx2) - influence);
			int bMaxX = Math.min(width - 1, Math.max(sx1, sx2) + influence);
			int bMinZ = Math.max(0, Math.min(sz1, sz2) - influence);
			int bMaxZ = Math.min(depth - 1, Math.max(sz1, sz2) + influence);

			for (int tx = bMinX; tx <= bMaxX; tx++) {
				for (int tz = bMinZ; tz <= bMaxZ; tz++) {
					double dist = distToSegment(tx, tz, sx1, sz1, sx2, sz2);
						if (dist < nearestDist[tx][tz]) nearestDist[tx][tz] = dist;
					if (dist < pathDistArr[tx][tz]) pathDistArr[tx][tz] = dist;
				}
			}
		}

		long distTime = System.currentTimeMillis() - phaseStart;
		Bukkit.getLogger().info("[MountainPath/FW] Distance maps: " + distTime + "ms");
		phaseStart = System.currentTimeMillis();

		// 5. Generate terrain via WorldEdit EditSession for bulk placement
		int blocksPlaced = 0;
		try (EditSession editSession = WorldEdit.getInstance().newEditSession(Region.world)) {
			for (int lx = 0; lx < width; lx++) {
				for (int lz = 0; lz < depth; lz++) {
					if (pieceHeightMap[lx][lz] != Integer.MIN_VALUE) continue;
					double dist = nearestDist[lx][lz];
					if (dist > terrainRadius) continue;

					int tx = startTX + lx;
					int tz = startTZ + lz;
					int worldX = -(tx + xOff + MapPieceInstance.X_FIGHT_OFFSET);
					int worldZ = tz + zOff + MapPieceInstance.Z_FIGHT_OFFSET;

					double pathDist = Double.MAX_VALUE;
					double pathYOff = 0;
					for (int[] seg : pathSegments) {
						double d = distToSegment(lx, lz, seg[0], seg[1], seg[2], seg[3]);
						if (d < pathDist) {
							pathDist = d;
							double t = projectOntoSegment(lx, lz, seg[0], seg[1], seg[2], seg[3]);
							pathYOff = seg[4] + (seg[5] - seg[4]) * t;
						}
					}

					double elevation = computeElevation(noise, tx, tz, pathDist);

					// Feather mountains near pieces: scale from 40% at boundary to 100% at TRANSITION_WIDTH
					double pd = pieceDist[lx][lz];
					if (pd < TRANSITION_WIDTH) {
						double t = pd / TRANSITION_WIDTH;
						t = t * t * (3 - 2 * t);
						elevation *= 0.4 + 0.6 * t;
					}

					int yOffset;
					if (pathDist <= CORRIDOR_HALF_WIDTH) {
						double bump = noise.noise(tx * 0.15, tz * 0.15) * 1.5;
						yOffset = (int) Math.round(pathYOff + bump);
					} else {
						yOffset = (int) Math.round(nearestPieceY[lx][lz]) + 2;
					}

					blocksPlaced += placeColumn(editSession, worldX, worldZ, baseY + yOffset, noise, elevation, pathDist, tx, tz);
				}
			}

			long terrainTime = System.currentTimeMillis() - phaseStart;
			Bukkit.getLogger().info("[MountainPath/FW] Terrain blocks: " + terrainTime + "ms (" + blocksPlaced + " blocks)");
			phaseStart = System.currentTimeMillis();

			// 6. Generate 1-block-thick barrier wall and roof
			// Pre-compute which blocks are inside the barrier boundary
			boolean[][] insideBarrier = new boolean[width][depth];
			for (int lx = 0; lx < width; lx++) {
				for (int lz = 0; lz < depth; lz++) {
					insideBarrier[lx][lz] = pieceDist[lx][lz] <= pieceBarrierRadius
							|| pathDistArr[lx][lz] <= pathBarrierRadius;
				}
			}
			// Cap barrier wall height to just above max mountain height
			int barrierWallTop = baseY + MAX_MOUNTAIN_HEIGHT + 3;
			for (int lx = 0; lx < width; lx++) {
				for (int lz = 0; lz < depth; lz++) {
					if (!insideBarrier[lx][lz]) continue;
					// Wall: inside block with at least one outside neighbor
					boolean isWall = (lx == 0 || !insideBarrier[lx - 1][lz])
							|| (lx == width - 1 || !insideBarrier[lx + 1][lz])
							|| (lz == 0 || !insideBarrier[lx][lz - 1])
							|| (lz == depth - 1 || !insideBarrier[lx][lz + 1]);

					int tx = startTX + lx;
					int tz = startTZ + lz;
					int worldX = -(tx + xOff + MapPieceInstance.X_FIGHT_OFFSET);
					int worldZ = tz + zOff + MapPieceInstance.Z_FIGHT_OFFSET;

					if (isWall) {
						// Barrier wall column up to capped height (only replace air)
						for (int y = baseY; y <= barrierWallTop; y++) {
							try {
								BlockVector3 pos = BlockVector3.at(worldX, y, worldZ);
								if (editSession.getBlock(pos).getBlockType().getMaterial().isAir()) {
									editSession.setBlock(pos, WE_BARRIER);
									blocksPlaced++;
								}
							} catch (Exception ignored) {}
						}
					}

					// Roof at fixed Y-level (only replace air)
					try {
						BlockVector3 roofPos = BlockVector3.at(worldX, BARRIER_ROOF_Y, worldZ);
						if (editSession.getBlock(roofPos).getBlockType().getMaterial().isAir()) {
							editSession.setBlock(roofPos, WE_BARRIER);
							blocksPlaced++;
						}
					} catch (Exception ignored) {}
				}
			}

			long barrierTime = System.currentTimeMillis() - phaseStart;
			Bukkit.getLogger().info("[MountainPath/FW] Barriers: " + barrierTime + "ms");
			phaseStart = System.currentTimeMillis();
		}
		long flushTime = System.currentTimeMillis() - phaseStart;
		Bukkit.getLogger().info("[MountainPath/FW] EditSession flush: " + flushTime + "ms");
		long duration = System.currentTimeMillis() - startTime;
		Bukkit.getLogger().info("[MountainPath/FW] Total: " + blocksPlaced + " blocks in " + duration + "ms"
				+ " (" + width + "x" + depth + " area, " + pathSegments.size() + " paths)");
	}

	/**
	 * Place a vertical column of terrain blocks using WorldEdit EditSession.
	 * Fills full column from base to surface.
	 * Returns number of blocks placed.
	 */
	private static int placeColumn(EditSession editSession, int worldX, int worldZ, int baseY,
			SimplexNoise noise, double elevation, double distToPath,
			int tx, int tz) {
		int surfaceY = baseY + (int) elevation;
		int placed = 0;

		try {
			if (elevation < 0.5) {
				// Path zone: flat surface with solid foundation below
				for (int y = baseY - 4; y <= baseY; y++) {
					editSession.setBlock(BlockVector3.at(worldX, y, worldZ), WE_PACKED_ICE);
					placed++;
				}
				// Railing/wall if right at edge of corridor
				if (distToPath > CORRIDOR_HALF_WIDTH - 0.5 && distToPath <= CORRIDOR_HALF_WIDTH) {
					editSession.setBlock(BlockVector3.at(worldX, baseY + 1, worldZ), WE_SNOW_BLOCK);
					placed++;
				}
			} else {
				// Fill full column from below base to surface (ensures no gaps at transitions)
				int fillStart = Math.min(baseY - 4, surfaceY);
				double stoneVar = noise.noise(tx * 0.1, tz * 0.1);
				for (int y = fillStart; y <= surfaceY; y++) {
					BaseBlock block;
					if (y == surfaceY) {
						block = WE_SNOW_BLOCK;
					} else if (y > surfaceY - 3) {
						block = elevation > 15 ? WE_PACKED_ICE : WE_STONE;
					} else {
						if (stoneVar > 0.3) block = WE_STONE;
						else if (stoneVar > 0) block = WE_ANDESITE;
						else block = WE_COBBLESTONE;
					}
					editSession.setBlock(BlockVector3.at(worldX, y, worldZ), block);
					placed++;
				}
			}
		} catch (Exception ignored) {}

		return placed;
	}
}
