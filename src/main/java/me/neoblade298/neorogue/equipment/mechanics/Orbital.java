package me.neoblade298.neorogue.equipment.mechanics;

import org.bukkit.block.Block;

import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.FightData;

public abstract class Orbital {
	private double rotationsPerSecond, ringRadius, duration, initialY = 1, initialRotation;
	private double width = 0.2, height = 0.2;
	private int pierce;
	private boolean ignoreBarriers, ignoreBlocks, ignoreEntities;

	public Orbital(double rotationsPerSecond, double ringRadius, double duration) {
		this.rotationsPerSecond = rotationsPerSecond;
		this.ringRadius = ringRadius;
		this.duration = duration;
	}

	public Orbital speed(double rotationsPerSecond) {
		this.rotationsPerSecond = rotationsPerSecond;
		return this;
	}

	public Orbital radius(double ringRadius) {
		this.ringRadius = ringRadius;
		return this;
	}

	public Orbital duration(double duration) {
		this.duration = duration;
		return this;
	}

	public Orbital initialY(double initialY) {
		this.initialY = initialY;
		return this;
	}

	public Orbital initialRotation(double initialRotation) {
		this.initialRotation = initialRotation;
		return this;
	}

	public Orbital size(double width, double height) {
		this.width = width;
		this.height = height;
		return this;
	}

	public Orbital pierce(int pierce) {
		this.pierce = pierce;
		return this;
	}

	public Orbital ignore(boolean barriers, boolean blocks, boolean entities) {
		this.ignoreBarriers = barriers;
		this.ignoreBlocks = blocks;
		this.ignoreEntities = entities;
		return this;

	}

	public double getRotationsPerSecond() {
		return rotationsPerSecond;
	}

	public double getRingRadius() {
		return ringRadius;
	}

	public double getDuration() {
		return duration;
	}

	public double getInitialY() {
		return initialY;
	}

	public double getInitialRotation() {
		return initialRotation;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	public int getPierceLimit() {
		return pierce;
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

	public OrbitalInstance start(FightData owner) {
		OrbitalInstance orbital = new OrbitalInstance(this, owner);
		onStart(orbital);
		return orbital;
	}

	public void onStart(OrbitalInstance orbital) {}
	public abstract void onTick(OrbitalInstance orbital, int interpolation);
	public void onHitBlock(OrbitalInstance orbital, Block block) {}
	public void onFizzle(OrbitalInstance orbital) {}
	public abstract void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, OrbitalInstance orbital);
}