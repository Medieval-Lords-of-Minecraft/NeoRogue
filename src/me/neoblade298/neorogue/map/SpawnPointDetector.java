package me.neoblade298.neorogue.map;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Detects valid spawn points in procedurally generated terrain
 * Categorizes spawns as player-suitable or mob-suitable based on accessibility
 */
public class SpawnPointDetector {
    private World world;
    private List<Location> validPlayerSpawns = new ArrayList<>();
    private List<Location> validMobSpawns = new ArrayList<>();
    
    /**
     * Scan a region for valid spawn points
     * @param world The world to scan
     * @param xMin Minimum X coordinate
     * @param xMax Maximum X coordinate
     * @param zMin Minimum Z coordinate
     * @param zMax Maximum Z coordinate
     */
    public void detectSpawnPoints(World world, int xMin, int xMax, int zMin, int zMax) {
        this.world = world;
        validPlayerSpawns.clear();
        validMobSpawns.clear();
        
        // Sample every 2 blocks for performance
        for (int x = xMin; x < xMax; x += 2) {
            for (int z = zMin; z < zMax; z += 2) {
                Location spawn = findValidSpawnAt(x, z);
                if (spawn != null) {
                    categorizeSpawn(spawn);
                }
            }
        }
    }
    
    /**
     * Find a valid spawn location at the given X,Z coordinates
     */
    private Location findValidSpawnAt(int x, int z) {
        // Scan from top down to find surface
        for (int y = 255; y >= 50; y--) {
            Block block = world.getBlockAt(x, y, z);
            Block above = world.getBlockAt(x, y + 1, z);
            Block above2 = world.getBlockAt(x, y + 2, z);
            
            // Check if valid spawn conditions:
            // 1. Solid block below
            // 2. Two air blocks above (for entity height)
            if (block.getType().isSolid() 
                && above.getType().isAir() 
                && above2.getType().isAir()) {
                
                Location loc = new Location(world, x + 0.5, y + 1, z + 0.5);
                
                // Additional validation
                if (isReachable(loc) && !isTooSteep(loc)) {
                    return loc;
                }
            }
        }
        return null;
    }
    
    /**
     * Check if location is reachable (not completely enclosed)
     */
    private boolean isReachable(Location loc) {
        int wallCount = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                Block check = loc.clone().add(dx, 0, dz).getBlock();
                if (check.getType().isSolid()) wallCount++;
            }
        }
        return wallCount < 6; // Not completely enclosed
    }
    
    /**
     * Check if slope is too steep for spawning
     */
    private boolean isTooSteep(Location loc) {
        int baseY = loc.getBlockY();
        int maxHeightDiff = 0;
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                
                int neighborY = findSurfaceY(loc.getBlockX() + dx, loc.getBlockZ() + dz);
                if (neighborY > 0) {
                    maxHeightDiff = Math.max(maxHeightDiff, Math.abs(neighborY - baseY));
                }
            }
        }
        
        return maxHeightDiff > 2; // Max 2 block climb
    }
    
    /**
     * Categorize spawn as player or mob suitable based on open space
     */
    private void categorizeSpawn(Location loc) {
        double openSpace = calculateOpenSpace(loc);
        
        if (openSpace > 40) {
            // Large open area - good for player spawn
            validPlayerSpawns.add(loc);
        }
        
        if (openSpace > 10) {
            // Smaller areas OK for mobs
            validMobSpawns.add(loc);
        }
    }
    
    /**
     * Calculate amount of open space around a location
     */
    private double calculateOpenSpace(Location loc) {
        int openCount = 0;
        int radius = 5;
        
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Location check = loc.clone().add(dx, 0, dz);
                Block block = check.getBlock();
                if (block.getType().isAir() || !block.getType().isSolid()) {
                    openCount++;
                }
            }
        }
        return openCount;
    }
    
    /**
     * Find the surface Y coordinate at given X,Z
     */
    private int findSurfaceY(int x, int z) {
        for (int y = 255; y >= 50; y--) {
            if (world.getBlockAt(x, y, z).getType().isSolid()) {
                return y;
            }
        }
        return -1;
    }
    
    public List<Location> getPlayerSpawns() {
        return validPlayerSpawns;
    }
    
    public List<Location> getMobSpawns() {
        return validMobSpawns;
    }
}
