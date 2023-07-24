package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.particles.Rectangle;

public class Barrier {
	private Player p;
	private double height, width, distanceFromPlayer;
	private BoundingBox bounds;
	Location bottomLeft, topRight;
	Vector cubeAxis, localX, localZ, localY = new Vector(0, 1, 0);
	
	public Barrier(Player p, double height, double width, double distanceFromPlayer) {
		this.p = p;
		this.height = height;
		this.width = width;
		this.distanceFromPlayer = distanceFromPlayer;
	}
	
	public void tick() {
		new Rectangle(p, width, height, distanceFromPlayer).draw(p, false, Particle.END_ROD, null);
		localZ = p.getEyeLocation().getDirection().setY(0).normalize();
		localX = localZ.clone().rotateAroundY(Math.PI);
		
		Vector left = localX.clone().multiply(-width / 2);
		Vector forward = localZ.clone().multiply(distanceFromPlayer);
		
		bottomLeft = p.getLocation().add(left);
		topRight = bottomLeft.add(left.multiply(-2));
		topRight.add(forward);
		topRight.add(0, height, 0);
		
		cubeAxis = topRight.subtract(bottomLeft).toVector();
	}
	
	public boolean collides(Location loc) {
		Location midpoint = bottomLeft.add(cubeAxis.clone().multiply(0.5));
		
		Vector v = loc.subtract(midpoint).toVector();
		double vx = Math.abs(v.dot(localX));
		double vy = Math.abs(v.dot(localY));
		double vz = Math.abs(v.dot(localZ));
		
		return 2 * vx < width && 2 * vy < height && 2 * vz < distanceFromPlayer;
	}
}
