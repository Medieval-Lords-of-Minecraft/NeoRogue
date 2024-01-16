package me.neoblade298.neorogue.equipment.mechanics;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;

public abstract class ProjectileSettings {
	private Location loc;
	private double gravity, yRotate, blocksPerTick;
	private boolean pierce, ignoreBarriers, ignoreBlocks, ignoreEntities;
	private int maxTicks, tickSpeed;
	private BoundingBox bounds = BoundingBox.of(loc, 0.2, 0.2, 0.2);
	public ProjectileSettings(double blocksPerTick, double maxRange, int tickSpeed) {
		this.blocksPerTick = blocksPerTick;
		this.tickSpeed = tickSpeed;
		this.maxTicks = (int) (maxRange / blocksPerTick) + 1;
	}
	
	public BoundingBox getBounds() {
		return bounds;
	}
	
	public int getTickSpeed() {
		return tickSpeed;
	}
	
	public double getBlocksPerTick() {
		return blocksPerTick;
	}
	
	public int getMaxTicks() {
		return maxTicks;
	}
	
	protected double getRotation() {
		return yRotate;
	}
	
	protected ProjectileSettings rotation(double yRotate) {
		this.yRotate = yRotate;
		return this;
	}
	
	protected ProjectileSettings gravity(double gravity) {
		this.gravity = gravity;
		return this;
	}
	
	protected ProjectileSettings pierce() {
		this.pierce = true;
		return this;
	}
	
	protected ProjectileSettings ignore(boolean barriers, boolean blocks, boolean entities) {
		this.ignoreBarriers = barriers;
		this.ignoreBlocks = blocks;
		this.ignoreEntities = entities;
		return this;
	}
	
	protected ProjectileSettings size(double width, double height) {
		bounds = BoundingBox.of(loc, width, height, width);
		return this;
	}
	
	protected boolean isPiercing() {
		return pierce;
	}
	
	protected double getGravity() {
		return gravity;
	}

	protected boolean isIgnoreBarriers() {
		return ignoreBarriers;
	}

	protected boolean isIgnoreBlocks() {
		return ignoreBlocks;
	}

	protected boolean isIgnoreEntities() {
		return ignoreEntities;
	}

	public Projectile start(FightInstance inst, FightData owner) {
		return new Projectile(this, inst, owner);
	}
	public abstract void onTick(Location loc);
	public abstract void onEnd(Location loc);
	public abstract void onHit(FightData hit, Barrier hitBarrier, Projectile proj);
}
