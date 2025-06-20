package me.neoblade298.neorogue.session.fight.buff;

import me.neoblade298.neorogue.session.fight.FightData;

public class Buff {
	private FightData applier;
	private double increase, multiplier;
	private BuffStatTracker tracker;
	
	public Buff() {}
	
	public Buff(FightData applier, double increase, double multiplier, BuffStatTracker tracker) {
		this.applier = applier;
		this.increase = increase;
		this.multiplier = multiplier;
		this.tracker = tracker;
	}

	public static Buff increase(FightData applier, double increase, BuffStatTracker tracker) {
		return new Buff(applier, increase, 0, tracker);
	}

	public static Buff multiplier(FightData applier, double multiplier, BuffStatTracker tracker) {
		return new Buff(applier, 0, multiplier, tracker);
	}

	public static Buff empty(FightData applier, BuffStatTracker tracker) {
		return new Buff(applier, 0, 0, tracker);
	}
	
	// Used in clone
	private Buff(Buff src) {
		this.applier = src.applier;
		this.increase = src.increase;
		this.multiplier = src.multiplier;
		this.tracker = src.tracker;
	}

	// Consider removing so that buffs are immutable
	// Currently this is used to keep similar buffs combined when adding them together
	public Buff replace(Buff other) {
		this.increase = other.increase;
		this.multiplier = other.multiplier;
		return this;
	}
	public Buff combine(Buff other) {
		this.increase += other.increase;
		this.multiplier += other.multiplier;
		return this;
	}

	public boolean isSimilar(Buff other) {
		return tracker.isSimilar(other.tracker) && applier == other.applier;
	}
	
	public Buff clone() {
		return new Buff(this);
	}

	public Buff invert() {
		return new Buff(applier, -increase, -multiplier, tracker);
	}

	public BuffStatTracker getStatTracker() {
		return tracker;
	}
	
	public double getIncrease() {
		return increase;
	}
	
	public double getMultiplier() {
		return multiplier;
	}

	public boolean isPositive(double base) {
		if (increase == 0) return multiplier > 0;
		else if (multiplier == 0) return increase > 0;
		else return (base * multiplier) + increase > 0;
	}

	public double getEffectiveChange(double base) {
		return (base * multiplier) + increase;
	}

	public FightData getApplier() {
		return applier;
	}
	
	@Override
	public String toString() {
		return increase + "," + multiplier;
	}
}
