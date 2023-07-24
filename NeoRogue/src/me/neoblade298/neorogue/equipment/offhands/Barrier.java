package me.neoblade298.neorogue.equipment.offhands;


import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.particles.Rectangle;

public class Barrier {
	private Player p;
	private double height, width, distanceFromPlayer;
	Location bottomLeft, topRight, midpoint;
	Vector cubeAxis, localX, localZ, localY = new Vector(0, height, 0);
	
	public Barrier(Player p, double height, double width, double distanceFromPlayer) {
		this.p = p;
		this.height = height;
		this.width = width;
		this.distanceFromPlayer = distanceFromPlayer;
	}
	
	public void tick() {
		new Rectangle(p, width, height, distanceFromPlayer - 2, 1).draw(p, true, Particle.END_ROD, null);
		localZ = p.getEyeLocation().getDirection().setY(0).normalize();
		localX = localZ.clone().rotateAroundY(Math.PI / 2);
		
		Vector left = localX.clone().multiply(-width / 2);
		Vector forward = localZ.clone().multiply(distanceFromPlayer);
		
		bottomLeft = p.getLocation().clone().add(left);
		topRight = bottomLeft.clone().add(left.multiply(-2));
		topRight.add(forward);
		topRight.add(0, height, 0);
		
		cubeAxis = topRight.clone().subtract(bottomLeft).toVector();
		
		localX.multiply(distanceFromPlayer);
		localZ.multiply(width);
		midpoint = bottomLeft.clone().add(cubeAxis.clone().multiply(0.5));
	}
	
	public boolean collides(Location loc) {
		if (midpoint == null) return false; // Shield has not ticked yet
		Vector v = loc.clone().subtract(midpoint).toVector();
		double vx = Math.abs(v.dot(localX));
		double vy = Math.abs(v.dot(localY));
		double vz = Math.abs(v.dot(localZ));
		return vx < width * 2 && vy < height * 2&& vz < distanceFromPlayer * 2;
	}
}
