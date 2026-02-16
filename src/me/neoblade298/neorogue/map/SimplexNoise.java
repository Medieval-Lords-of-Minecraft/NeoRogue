package me.neoblade298.neorogue.map;

import java.util.Random;

/**
 * Simplex noise implementation for procedural terrain generation
 * Based on Ken Perlin's improved noise algorithm
 */
public class SimplexNoise {
    private static final int[][] grad3 = {
        {1,1,0},{-1,1,0},{1,-1,0},{-1,-1,0},
        {1,0,1},{-1,0,1},{1,0,-1},{-1,0,-1},
        {0,1,1},{0,-1,1},{0,1,-1},{0,-1,-1}
    };
    
    private static final int[] p = new int[512];
    private static final int[] perm = new int[512];
    
    public SimplexNoise(long seed) {
        Random rand = new Random(seed);
        
        // Generate permutation table
        for (int i = 0; i < 256; i++) {
            p[i] = i;
        }
        
        // Shuffle using Fisher-Yates
        for (int i = 255; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int temp = p[i];
            p[i] = p[j];
            p[j] = temp;
        }
        
        // Duplicate for overflow
        for (int i = 0; i < 512; i++) {
            perm[i] = p[i & 255];
        }
    }
    
    private static double dot(int[] g, double x, double y) {
        return g[0] * x + g[1] * y;
    }
    
    /**
     * 2D Simplex noise
     * @param xin X coordinate
     * @param yin Y coordinate
     * @return Noise value between -1 and 1
     */
    public double noise(double xin, double yin) {
        double n0, n1, n2;
        
        // Skew input space to determine which simplex cell we're in
        final double F2 = 0.5 * (Math.sqrt(3.0) - 1.0);
        double s = (xin + yin) * F2;
        int i = fastFloor(xin + s);
        int j = fastFloor(yin + s);
        
        final double G2 = (3.0 - Math.sqrt(3.0)) / 6.0;
        double t = (i + j) * G2;
        double X0 = i - t;
        double Y0 = j - t;
        double x0 = xin - X0;
        double y0 = yin - Y0;
        
        // Determine which simplex we're in
        int i1, j1;
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } else {
            i1 = 0;
            j1 = 1;
        }
        
        // Offsets for corners
        double x1 = x0 - i1 + G2;
        double y1 = y0 - j1 + G2;
        double x2 = x0 - 1.0 + 2.0 * G2;
        double y2 = y0 - 1.0 + 2.0 * G2;
        
        // Work out hashed gradient indices
        int ii = i & 255;
        int jj = j & 255;
        int gi0 = perm[ii + perm[jj]] % 12;
        int gi1 = perm[ii + i1 + perm[jj + j1]] % 12;
        int gi2 = perm[ii + 1 + perm[jj + 1]] % 12;
        
        // Calculate contribution from three corners
        double t0 = 0.5 - x0 * x0 - y0 * y0;
        if (t0 < 0) {
            n0 = 0.0;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * dot(grad3[gi0], x0, y0);
        }
        
        double t1 = 0.5 - x1 * x1 - y1 * y1;
        if (t1 < 0) {
            n1 = 0.0;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * dot(grad3[gi1], x1, y1);
        }
        
        double t2 = 0.5 - x2 * x2 - y2 * y2;
        if (t2 < 0) {
            n2 = 0.0;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * dot(grad3[gi2], x2, y2);
        }
        
        // Sum up and scale to [-1, 1]
        return 70.0 * (n0 + n1 + n2);
    }
    
    private static int fastFloor(double x) {
        int xi = (int) x;
        return x < xi ? xi - 1 : xi;
    }
}
