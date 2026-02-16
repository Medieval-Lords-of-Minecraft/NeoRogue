package me.neoblade298.neorogue.map;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Generates procedural snowy mountain terrain using multi-octave Simplex noise
 */
public class ProceduralMountainGenerator {
    private final Random random;
    private final SimplexNoise noise;
    private final long seed;
    
    // Generation parameters
    private int baseHeight = 64;
    private int maxHeight = 35;
    private double scale = 0.015;  // Controls "zoom" of terrain features
    private boolean addCaves = false;
    
    public ProceduralMountainGenerator(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
        this.noise = new SimplexNoise(seed);
    }
    
    /**
     * Set generation parameters
     */
    public ProceduralMountainGenerator setHeight(int base, int variation) {
        this.baseHeight = base;
        this.maxHeight = variation;
        return this;
    }
    
    public ProceduralMountainGenerator setScale(double scale) {
        this.scale = scale;
        return this;
    }
    
    public ProceduralMountainGenerator setCaves(boolean enabled) {
        this.addCaves = enabled;
        return this;
    }
    
    /**
     * Generate mountain terrain in the specified region
     * @param world Target world
     * @param xOffset X offset in blocks
     * @param zOffset Z offset in blocks
     * @param size Size of area to generate (in blocks)
     */
    public void generate(World world, int xOffset, int zOffset, int size) {
        Bukkit.getLogger().info("[ProceduralMountain] Generating " + size + "x" + size + " mountain at " 
            + xOffset + "," + zOffset + " with seed " + seed);
        
        long startTime = System.currentTimeMillis();
        int blocksPlaced = 0;
        
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                int worldX = xOffset + x;
                int worldZ = zOffset + z;
                
                // Get terrain elevation
                double elevation = getElevation(x, z);
                int height = baseHeight + (int)(elevation * maxHeight);
                
                // Get cave noise if enabled
                SimplexNoise caveNoise = addCaves ? new SimplexNoise(seed + 1) : null;
                
                // Build column from bottom to top
                for (int y = 0; y <= height + 5; y++) {
                    Location loc = new Location(world, worldX, y, worldZ);
                    Block block = loc.getBlock();
                    
                    // Check if we should carve out a cave
                    if (addCaves && y < height - 5 && shouldCarveCave(caveNoise, x, y, z)) {
                        block.setType(Material.AIR);
                        continue;
                    }
                    
                    // Determine block type based on height
                    if (y > height) {
                        // Above terrain - clear it
                        if (!block.getType().isAir()) {
                            block.setType(Material.AIR);
                        }
                    } else if (y == height) {
                        // Top layer - snow
                        block.setType(Material.SNOW_BLOCK);
                        blocksPlaced++;
                    } else if (y > height - 3) {
                        // Near surface - vary between stone and packed ice
                        if (elevation > 0.65) {
                            block.setType(Material.PACKED_ICE);
                        } else if (elevation > 0.5) {
                            block.setType(Material.STONE);
                        } else {
                            block.setType(Material.COBBLESTONE);
                        }
                        blocksPlaced++;
                    } else {
                        // Inner - mostly stone with some variation
                        double stoneVariation = noise.noise(x * 0.1, z * 0.1);
                        if (stoneVariation > 0.3) {
                            block.setType(Material.STONE);
                        } else if (stoneVariation > 0) {
                            block.setType(Material.ANDESITE);
                        } else {
                            block.setType(Material.COBBLESTONE);
                        }
                        blocksPlaced++;
                    }
                }
            }
        }
        
        long duration = System.currentTimeMillis() - startTime;
        Bukkit.getLogger().info("[ProceduralMountain] Generated " + blocksPlaced + " blocks in " + duration + "ms");
    }
    
    /**
     * Calculate elevation using multi-octave noise
     * Returns value between 0 and 1
     */
    private double getElevation(int x, int z) {
        // Multi-octave noise for natural terrain
        // Each octave adds detail at different scales
        double e = 0;
        
        // Large features (mountains)
        e += noise.noise(x * scale, z * scale) * 0.5;
        
        // Medium features (hills)
        e += noise.noise(x * scale * 2, z * scale * 2) * 0.25;
        
        // Small features (detail)
        e += noise.noise(x * scale * 4, z * scale * 4) * 0.125;
        
        // Tiny features (texture)
        e += noise.noise(x * scale * 8, z * scale * 8) * 0.0625;
        
        // Normalize to 0-1 range
        // Total amplitude: 0.5 + 0.25 + 0.125 + 0.0625 = 0.9375
        // Noise range is roughly -0.9375 to 0.9375
        e = (e + 1) / 2;
        
        // Apply power curve to make peaks sharper and valleys wider
        e = Math.pow(e, 1.3);
        
        return Math.max(0, Math.min(1, e));
    }
    
    /**
     * Determine if a cave should be carved at this position
     */
    private boolean shouldCarveCave(SimplexNoise caveNoise, int x, int y, int z) {
        if (caveNoise == null) return false;
        
        // 3D noise for caves (simulate with 2D by using y as part of coordinates)
        double cave = caveNoise.noise(x * 0.05, (y + z) * 0.05);
        return cave > 0.4; // Threshold for cave carving
    }
    
    /**
     * Add decorative elements to the terrain (trees, rocks, etc)
     */
    public void addDecorations(World world, int xOffset, int zOffset, int size) {
        // Find snow-covered surfaces and add decorations
        for (int i = 0; i < size * 2; i++) {
            int x = random.nextInt(size);
            int z = random.nextInt(size);
            
            // Find surface
            for (int y = 255; y >= baseHeight; y--) {
                Block block = world.getBlockAt(xOffset + x, y, zOffset + z);
                if (block.getType() == Material.SNOW_BLOCK) {
                    Block above = block.getRelative(0, 1, 0);
                    
                    // Random decoration choice
                    double rand = random.nextDouble();
                    if (rand > 0.7) {
                        // Small ice spike
                        above.setType(Material.PACKED_ICE);
                        if (random.nextDouble() > 0.5) {
                            above.getRelative(0, 1, 0).setType(Material.PACKED_ICE);
                        }
                    } else if (rand > 0.5) {
                        // Snow layer
                        above.setType(Material.SNOW);
                    }
                    break;
                }
            }
        }
    }
}
