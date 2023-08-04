package me.neoblade298.neorogue.session;

import org.bukkit.Location;

public class Plot {
	private int x, z;
	
	private static int PLOT_X_SIZE = 320, PLOT_Z_SIZE = 320;
	
	public Plot(int x, int z) {
		this.x = x;
		this.z = z;
	}
	
	public static Plot locationToPlot(Location loc) {
		return new Plot(loc.getBlockX() / PLOT_X_SIZE, loc.getBlockZ() / PLOT_Z_SIZE);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Plot) {
			Plot pl = (Plot) o;
			return pl.x == this.x && pl.z == this.z;
		}
		return false;
	}
	
	public int getXOffset() {
		return x;
	}
	
	public int getZOffset() {
		return z;
	}
	
	@Override
	public String toString() {
		return x + "," + z;
	}
}
