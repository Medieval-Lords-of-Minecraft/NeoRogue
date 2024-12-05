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

	public static Buff increase(FightData applier, double increase) {
		return new Buff(applier, increase, 0, BuffOrigin.NORMAL);
	}

	public static Buff increase(FightData applier, double increase, BuffOrigin origin) {
		return new Buff(applier, increase, 0, origin);
	}

	public static Buff multiplier(FightData applier, double multiplier) {
		return new Buff(applier, 0, multiplier, BuffOrigin.NORMAL);
	}

	public static Buff multiplier(FightData applier, double multiplier, BuffOrigin origin) {
		return new Buff(applier, 0, multiplier, origin);
	}
	
	// Used in clone
	private Buff(Buff src) {
		this.applier = src.applier;
		this.increase = src.increase;
		this.multiplier = src.multiplier;
		this.origin = src.origin;
	}

	public void setOrigin(BuffOrigin origin) {
		this.origin = origin;
	}

	public Buff combine(Buff other) {
		this.increase += other.increase;
		this.multiplier += other.multiplier;
		return this;
	}

	public boolean isSimilar(Buff other) {
		return origin == other.origin && applier == other.applier;
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

	public double getEffectiveChange(double base) {
		return (base * multiplier) + increase;
	}
	
	@Override
	public String toString() {
		return increase + "," + multiplier;
	}
}
