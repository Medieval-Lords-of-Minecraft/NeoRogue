package me.neoblade298.neorogue.equipment.mechanics;

import me.neoblade298.neorogue.session.fight.FightData;

public abstract class Projectile {
	private double gravity, yRotate, blocksPerTick;
	private boolean pierce, ignoreBarriers, ignoreBlocks, ignoreEntities;
	private double width = 0.2, height = 0.2;
	private int maxTicks, tickSpeed;
	public Projectile(double blocksPerTick, double maxRange, int tickSpeed) {
		this.blocksPerTick = blocksPerTick;
		this.tickSpeed = tickSpeed;
		this.maxTicks = (int) (maxRange / blocksPerTick) + 1;
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
	
	public int getMaxTicks() {
		return maxTicks;
	}
	
	protected double getRotation() {
		return yRotate;
	}
	
	protected Projectile rotation(double yRotate) {
		this.yRotate = yRotate;
		return this;
	}
	
	protected Projectile gravity(double gravity) {
		this.gravity = gravity;
		return this;
	}
	
	protected Projectile pierce() {
		this.pierce = true;
		return this;
	}
	
	protected Projectile ignore(boolean barriers, boolean blocks, boolean entities) {
		this.ignoreBarriers = barriers;
		this.ignoreBlocks = blocks;
		this.ignoreEntities = entities;
		return this;
	}
	
	protected Projectile size(double width, double height) {
		this.width = width;
		this.height = height;
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

	// Can only be run by ProjectileGroup
	protected ProjectileInstance start(FightData owner) {
		return new ProjectileInstance(this, owner);
	}
	public abstract void onTick(ProjectileInstance proj);
	public abstract void onEnd(ProjectileInstance proj);
	public abstract void onHit(FightData hit, Barrier hitBarrier, ProjectileInstance proj);
}
