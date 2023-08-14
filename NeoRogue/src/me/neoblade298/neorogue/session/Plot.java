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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + z;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Plot other = (Plot) obj;
		if (x != other.x) return false;
		if (z != other.z) return false;
		return true;
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
