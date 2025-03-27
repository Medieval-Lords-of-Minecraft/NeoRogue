package me.neoblade298.neorogue.equipment.mechanics;


import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleShapeMemory;
import me.neoblade298.neocore.bukkit.effects.Rectangle;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffList;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;

public class Barrier {
	private static final double METERS_PER_PARTICLE = 0.5,
			FORWARD_OFFSET = 0.5; // Used so the shield hitbox doesn't protect the user from side attacks, only for centered
	private static final ParticleContainer DEFAULT_SHIELD_PARTICLE = new ParticleContainer(Particle.END_ROD).count(1);
	
	// Shared
	private UUID uuid;
	private LivingEntity owner;
	private double length, height, forward; // Forward is used for where the rectangle actually is drawn
	private ParticleContainer part;
	private Rectangle rect;
	private Location center, rectcenter; // Center is midpoint of barrier, rectcenter is midpoint of actually rectangle to draw
	private HashMap<DamageBuffType, BuffList> buffs;
	private boolean isUnbreakable; // If true, this barrier cancels damage instead of applying buffs
	
	// Stationary
	private ParticleShapeMemory mem;
	
	// Centered on owner
	private LocalAxes axes;
	
	private Barrier(LivingEntity owner, double length, double forward, double height, HashMap<DamageBuffType, BuffList> buffs, ParticleContainer part, boolean isUnbreakable) {
		this.uuid = UUID.randomUUID();
		this.owner = owner;
		this.height = height;
		this.length = length;
		this.forward = forward;
		this.rect = new Rectangle(length, height, METERS_PER_PARTICLE);
		this.buffs = buffs != null ? buffs : new HashMap<DamageBuffType, BuffList>();
		this.part = part != null ? part : DEFAULT_SHIELD_PARTICLE;
		this.isUnbreakable = isUnbreakable;
		calculateLocation();
	}
	
	// Stationary version
	private Barrier(LivingEntity owner, double length, double forward, double height, Location center, LocalAxes axes,
		HashMap<DamageBuffType, BuffList> buffs, ParticleContainer part, boolean isUnbreakable) {
		this(owner, length, forward, height, buffs, part, isUnbreakable);
		this.center = center;
		this.mem = rect.calculate(center, axes);
	}
	
	public static Barrier stationary(LivingEntity owner, double length, double forward, double height, Location center, LocalAxes axes,
		HashMap<DamageBuffType, BuffList> buffs, @Nullable ParticleContainer part, boolean isUnbreakable) {
		return new Barrier(owner, length, forward, height, center, axes, buffs, part, isUnbreakable);
	}
	
	public static Barrier centered(LivingEntity owner, double length, double forward, double height, double forwardOffset, 
		HashMap<DamageBuffType, BuffList> buffs, @Nullable ParticleContainer part, boolean isUnbreakable) {
		return new Barrier(owner, length, forward, height, buffs, part, isUnbreakable);
	}
	
	public void tick() {
		// Static tick
		if (mem != null) {
			if (owner instanceof Player) {
				mem.play((Player) owner, part, part);
			}
			else {
				mem.play(part, part);
			}
		}
		else {
			calculateLocation();
			if (owner instanceof Player) {
				rect.play((Player) owner, part, center, axes, part);
			}
			else {
				rect.play(part, rectcenter, axes, part);
			}
		}
	}
	
	private void calculateLocation() {
		axes = LocalAxes.usingEyeLocation(owner);
		center = owner.getLocation().add(axes.forward().multiply((FORWARD_OFFSET + forward) / 2)).add(axes.up().multiply(height / 2));
		rectcenter = center.clone().add(axes.forward().multiply((FORWARD_OFFSET + forward) / 2));
	}

	public Location getLocation() {
		return center;
	}
	
	public boolean collides(Location loc) {
		if (mem == null) {
			if (owner.getLocation().distanceSquared(loc) > 200) return false; // Optimization, too far from shield to possibly collide
		}
		else {
			if (center.distanceSquared(loc) > 200) return false;
		}

		if (!center.getWorld().equals(loc.getWorld())) {
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

	public void addBuff(DamageBuffType type, Buff b) {
		BuffList list = buffs.getOrDefault(type, new BuffList());
		list.add(b);
		buffs.put(type, list);
	}
	
	public HashMap<DamageBuffType, BuffList> getBuffLists() {
		return buffs;
	}

	public boolean isUnbreakable() {
		return isUnbreakable;
	}
}
