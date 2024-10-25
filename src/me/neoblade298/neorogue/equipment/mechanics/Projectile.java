package me.neoblade298.neorogue.equipment.mechanics;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.session.fight.FightData;

public abstract class Projectile extends IProjectile {
	private double gravity, yRotate, blocksPerTick, initialY, maxRange, homing;
	private boolean ignoreBarriers, ignoreBlocks, ignoreEntities;
	private double width = 0.2, height = 0.2;
	private int tickSpeed, pierce = 1;

	public Projectile(double blocksPerTick, double maxRange, int tickSpeed) {
		this.blocksPerTick = blocksPerTick; // Per in-game tick, not projectile tick
		this.tickSpeed = tickSpeed;
		this.maxRange = maxRange;
	}

	public Projectile(double maxRange, int tickSpeed) {
		this(1, maxRange, tickSpeed);
	}

	public void setBowDefaults() {
		this.gravity = 0.005;
		this.width = 0.2;
		this.height = 0.2;
		this.blocksPerTick = 3;
	}

	public double getMaxRange() {
		return maxRange;
	}
	
	public double getWidth() {
		return width;
	}
	
	public double getHeight() {
		return height;
	}
	
	public int getTickSpeed() {
		return tickSpeed;
	}
	
	public double getBlocksPerTick() {
		return blocksPerTick;
	}
	
	public double getRotation() {
		return yRotate;
	}

	public Projectile blocksPerTick(double blocksPerTick) {
		this.blocksPerTick = blocksPerTick;
		return this;
	}
	
	public Projectile initialY(double initialY) {
		this.initialY = initialY;
		return this;
	}
	
	public Projectile rotation(double yRotate) {
		this.yRotate = yRotate;
		return this;
	}
	
	public Projectile gravity(double gravity) {
		this.gravity = gravity;
		return this;
	}

	public Projectile homing(double homing) {
		this.homing = homing;
		return this;
	}
	
	public Projectile pierce(int num) {
		this.pierce = num;
		return this;
	}
	
	public Projectile ignore(boolean barriers, boolean blocks, boolean entities) {
		this.ignoreBarriers = barriers;
		this.ignoreBlocks = blocks;
		this.ignoreEntities = entities;
		return this;
	}
	
	public Projectile size(double width, double height) {
		this.width = width;
		this.height = height;
		return this;
	}
	
	public int getPierceLimit() {
		return pierce;
	}
	
	public double getGravity() {
		return gravity;
	}

	public double getHoming() {
		return homing;
	}
	
	public double initialY() {
		return initialY;
	}

	public boolean isIgnoreBarriers() {
		return ignoreBarriers;
	}

	public boolean isIgnoreBlocks() {
		return ignoreBlocks;
	}

	public boolean isIgnoreEntities() {
		return ignoreEntities;
	}
	
	@Override
	protected ProjectileInstance create(FightData owner, Location source, Vector direction) {
		ProjectileInstance proj = new ProjectileInstance(this, owner, source, direction);
		onStart(proj);
		return proj;
	}
	public void onStart(ProjectileInstance proj) {}
	public abstract void onTick(ProjectileInstance proj, int interpolation); // interpolation 0 == baseline, every number after is interpolation
	public void onHitBlock(ProjectileInstance proj, Block b) {}
	public void onFizzle(ProjectileInstance proj) {}
	public abstract void onHit(FightData hit, Barrier hitBarrier, ProjectileInstance proj);
}
