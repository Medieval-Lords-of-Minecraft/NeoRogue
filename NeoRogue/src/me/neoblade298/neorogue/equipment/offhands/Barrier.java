package me.neoblade298.neorogue.equipment.offhands;


import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.particles.Rectangle;
import me.neoblade298.neorogue.session.fights.Buff;
import me.neoblade298.neorogue.session.fights.BuffType;
import me.neoblade298.neorogue.session.fights.DamageType;

public class Barrier {
	private Player p;
	private double height, width, distanceFromPlayer;
	private Location bottomLeft, topRight, midpoint;
	private Vector cubeAxis, localX, localZ, localY = new Vector(0, height, 0);
	
	private HashMap<BuffType, Buff> buffs = new HashMap<BuffType, Buff>();
	
	public Barrier(Player p, double width, double distanceFromPlayer, double height, HashMap<BuffType, Buff> buffs) {
		this.p = p;
		this.height = height;
		this.width = width;
		this.distanceFromPlayer = distanceFromPlayer;
		this.buffs = buffs;
	}
	
	public void tick() {
		new Rectangle(p, width, height, distanceFromPlayer, 1).draw(p, true, Particle.END_ROD, null);
		localZ = p.getEyeLocation().getDirection().setY(0).normalize();
		localX = localZ.clone().rotateAroundY(Math.PI / 2);
		
		Vector left = localX.clone().multiply(-width / 2);
		Vector forward = localZ.clone().multiply(distanceFromPlayer + 2);
		
		bottomLeft = p.getLocation().clone().add(left);
		topRight = bottomLeft.clone().add(left.multiply(-2));
		topRight.add(forward);
		topRight.add(0, height, 0);
		
		cubeAxis = topRight.clone().subtract(bottomLeft).toVector();
		
		localX.multiply(width);
		localZ.multiply(distanceFromPlayer);
		midpoint = bottomLeft.clone().add(cubeAxis.clone().multiply(0.5));
	}
	
	public boolean collides(Location loc) {
		if (midpoint == null) return false; // Shield has not ticked yet
		Vector v = loc.clone().subtract(midpoint).toVector();
		if (v.lengthSquared() > 400) return false; // Optimization, too far from shield to possibly collide

		double vx = Math.abs(v.dot(localX));
		if (vx >= width * 2) return false;
		double vy = Math.abs(v.dot(localY));
		if (vy >= height * 2) return false;
		double vz = Math.abs(v.dot(localZ));
		return vz < distanceFromPlayer * 2;
	}
	
	public double applyDefenseBuffs(double amount, DamageType type) {
		double mult = 1;
		for (BuffType bt : type.getBuffTypes()) {
			if (!buffs.containsKey(bt)) continue;
			Buff b = buffs.get(bt);
			mult -= b.getMultiplier();
			amount -= b.getIncrease();
		}
		return amount * mult;
	}
}
