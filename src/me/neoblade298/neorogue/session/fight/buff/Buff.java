package me.neoblade298.neorogue.session.fight.buff;

import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.FightData;

public class Buff {
	private FightData applier;
	private double increase, multiplier;
	private BuffOrigin origin;
	
	public Buff() {}
	
	public Buff(FightData applier, double increase, double multiplier) {
		this(applier, increase, multiplier, BuffOrigin.NORMAL);
	}
	
	public Buff(FightData applier, double increase, double multiplier, BuffOrigin origin) {
		this.applier = applier;
		this.increase = increase;
		this.multiplier = multiplier;
		this.origin = origin;
	}
	
	// Used in clone
	private Buff(Buff src) {
		this.applier = src.applier;
		this.increase = src.increase;
		this.multiplier = src.multiplier;
		this.origin = src.origin;
	}

	public boolean isSimilar(Buff other) {
		return origin == other.origin;
	}
	
	public Buff clone() {
		return new Buff(this);
	}

	public Buff invert() {
		return new Buff(applier, -increase, -multiplier, origin);
	}

	public BuffOrigin getOrigin() {
		return origin;
	}
	
	public double getIncrease() {
		return increase;
	}
	
	public double getMultiplier() {
		return multiplier;
	}
	
	public double apply(double original) {
		return (original * (1 + multiplier)) + increase;
	}
	
	public double applyNegative(double original) {
		return (original * (1 - multiplier)) - increase;
	}
	
	public boolean isEmpty() {
		return increase == 0 && multiplier == 0;
	}
	
	@Override
	public String toString() {
		return increase + "," + multiplier;
	}
}
