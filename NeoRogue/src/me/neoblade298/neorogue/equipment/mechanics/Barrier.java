package me.neoblade298.neorogue.equipment.mechanics;


import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import me.neoblade298.neocore.bukkit.particles.LocalAxes;
import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.particles.ParticleShapeMemory;
import me.neoblade298.neocore.bukkit.particles.Rectangle;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;

public class Barrier {
	private static final double METERS_PER_PARTICLE = 0.5,
			FORWARD_OFFSET = 0.5; // Used so the shield hitbox doesn't protect the user from side attacks, only for centered
	private static final ParticleContainer DEFAULT_SHIELD_PARTICLE = new ParticleContainer(Particle.END_ROD).count(1);
	
	// Shared
	private UUID uuid;
	private LivingEntity owner;
	private double length, height, forward; // Forward is used for where the rectangle actually is drawn
	private HashMap<BuffType, Buff> buffs = new HashMap<BuffType, Buff>();
	private ParticleContainer part;
	private Rectangle rect;
	private Location center, rectcenter; // Center is midpoint of barrier, rectcenter is midpoint of actually rectangle to draw
	
	// Stationary
	private ParticleShapeMemory mem;
	
	// Centered on owner
	private LocalAxes axes;
	
	private Barrier(LivingEntity owner, double length, double forward, double height, HashMap<BuffType, Buff> buffs, ParticleContainer part) {
		this.uuid = UUID.randomUUID();
		this.owner = owner;
		this.height = height;
		this.length = length;
		this.forward = forward;
		this.buffs = buffs;
		this.rect = new Rectangle(length, height, METERS_PER_PARTICLE);
		
		if (part == null) {
			this.part = DEFAULT_SHIELD_PARTICLE;
		}
		else {
			this.part = part;
		}
	}
	
	// Stationary version
	private Barrier(LivingEntity owner, double length, double forward, double height, Location center, LocalAxes axes,
			HashMap<BuffType, Buff> buffs, ParticleContainer part) {
		this(owner, length, forward, height, buffs, part);
		this.center = center;
		this.mem = rect.calculate(center, axes);
	}
	
	public static Barrier stationary(LivingEntity owner, double length, double forward, double height, Location center, LocalAxes axes,
			HashMap<BuffType, Buff> buffs, @Nullable ParticleContainer part) {
		return new Barrier(owner, length, forward, height, buffs, part);
	}
	
	public static Barrier stationary(LivingEntity owner, double length, double forward, double height, Location center, LocalAxes axes,
			HashMap<BuffType, Buff> buffs) {
		return new Barrier(owner, length, forward, height, buffs, null);
	}
	
	public static Barrier centered(LivingEntity owner, double length, double forward, double height, double forwardOffset, 
			HashMap<BuffType, Buff> buffs) {
		return new Barrier(owner, length, forward, height, buffs, null);
	}
	
	public static Barrier centered(LivingEntity owner, double length, double forward, double height, double forwardOffset, 
			HashMap<BuffType, Buff> buffs, @Nullable ParticleContainer part) {
		return new Barrier(owner, length, forward, height, buffs, part);
	}
	
	public void tick() {
		// Static tick
		if (mem != null) {
			mem.draw(mem.calculateCache(), part, part);
		}
		else {
			axes = LocalAxes.usingEyeLocation(owner);
			center = owner.getLocation().add(axes.forward().multiply((FORWARD_OFFSET + forward) / 2)).add(axes.up().multiply(height / 2));
			rectcenter = center.clone().add(axes.forward().multiply((FORWARD_OFFSET + forward) / 2));
			rect.draw(part, rectcenter, axes, part);
		}
	}
	
	public boolean collides(Location loc) {
		if (mem == null) {
			if (owner.getLocation().distanceSquared(loc) > 200) return false; // Optimization, too far from shield to possibly collide
		}
		else {
			if (center.distanceSquared(loc) > 200) return false;
		}

		if (center.getWorld().equals(loc.getWorld())) {
			Bukkit.getLogger().warning("[NeoRogue] Barrier and projectile in different worlds: " + center.getWorld() + " and " + loc.getWorld());
			return false;
		}
		Vector v = loc.clone().subtract(center).toVector();
		double vx = Math.abs(v.dot(axes.left().multiply(length)));
		if (vx >= length * 2) return false;
		double vy = Math.abs(v.dot(axes.up().multiply(height)));
		if (vy >= height * 2) return false;
		double vz = Math.abs(v.dot(axes.forward().multiply(forward)));
		return vz < forward * 2;
	}

	public LivingEntity getOwner() {
		return this.owner;
	}
	
	public UUID getUniqueId() {
		return uuid;
	}
	
	public HashMap<BuffType, Buff> getBuffs() {
		return buffs;
	}
	
	public double applyDefenseBuffs(DamageType type, double amount) {
		return Buff.applyDefenseBuffs(buffs, type, amount);
	}
}
