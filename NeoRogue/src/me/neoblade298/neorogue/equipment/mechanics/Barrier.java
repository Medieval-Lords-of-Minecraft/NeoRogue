package me.neoblade298.neorogue.equipment.mechanics;


import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.particles.Rectangle;
import me.neoblade298.neorogue.session.fights.Buff;
import me.neoblade298.neorogue.session.fights.BuffType;
import me.neoblade298.neorogue.session.fights.DamageType;

public class Barrier {
	private LivingEntity owner;
	private double height, width, forward, forwardOffset;
	private Location bottomLeft, topRight, midpoint;
	private boolean needsUpdate = false, isStatic;
	private Vector cubeAxis, localX, localZ, localY;
	
	private Rectangle rect;
	private HashMap<BuffType, Buff> buffs = new HashMap<BuffType, Buff>();
	
	public Barrier(LivingEntity owner, double width, double forward, double height, double forwardOffset,
			HashMap<BuffType, Buff> buffs, boolean isStatic) {
		this.owner = owner;
		this.height = height;
		this.width = width;
		this.forward = forward;
		this.buffs = buffs;
		this.isStatic = isStatic;
		this.forwardOffset = forwardOffset;
		
		if (isStatic) {
			rect = new Rectangle(owner, width, height, forward, forwardOffset, 0.2);
			update(true);
		}
	}
	
	public void tick() {
		if (isStatic) {
			rect.drawEdges(null, true, Particle.END_ROD, null);
		}
		else {
			new Rectangle(owner, width, height, forward, forwardOffset, 0.2).drawEdges(null, true, Particle.END_ROD, null);
			needsUpdate = true;
		}
	}
	
	private void update(boolean force) {
		// localZ is calculated in collides
		if (isStatic && !force) return;
		if (force) {
			localZ = owner.getEyeLocation().getDirection().normalize();
		}
		localX = localZ.clone().setY(0).rotateAroundY(Math.PI / 2);
		localY = localZ.clone().rotateAroundAxis(localZ, -Math.PI / 2);
		
		Vector left = localX.clone().multiply(-width / 2);
		Vector forward = localZ.clone().multiply(this.forward + 2);
		
		bottomLeft = owner.getLocation().clone().add(left);
		topRight = bottomLeft.clone().add(left.multiply(-2));
		topRight.add(forward);
		topRight.add(0, height, 0);
		
		cubeAxis = topRight.clone().subtract(bottomLeft).toVector();
		
		localX.multiply(width);
		localZ.multiply(forward);
		midpoint = bottomLeft.clone().add(cubeAxis.clone().multiply(0.5));
	}
	
	public boolean collides(Location loc) {
		if (!isStatic) {
			localZ = owner.getEyeLocation().getDirection().normalize();
			Location approx = owner.getEyeLocation().add(localZ.clone().multiply(forward));
			if (approx.distanceSquared(loc) > 200) return false; // Optimization, too far from shield to possibly collide
		}
		else {
			if (midpoint.distanceSquared(loc) > 200) return false;
		}

		if (needsUpdate) update(false);
		Vector v = loc.clone().subtract(midpoint).toVector();
		double vx = Math.abs(v.dot(localX));
		if (vx >= width * 2) return false;
		double vy = Math.abs(v.dot(localY));
		if (vy >= height * 2) return false;
		double vz = Math.abs(v.dot(localZ));
		return vz < forward * 2;
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

	public LivingEntity getOwner() {
		return this.owner;
	}
}
