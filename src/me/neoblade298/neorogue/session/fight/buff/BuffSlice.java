package me.neoblade298.neorogue.session.fight.buff;

import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;

public class BuffSlice {
	private double increase, multiplier;
	private BuffOrigin origin;
	
	public BuffSlice() {}
	public BuffSlice(double increase, double multiplier, BuffOrigin origin) {
		this.increase = increase;
		this.multiplier = multiplier;
		this.origin = origin;
	}
	public BuffSlice clone() {
		return new BuffSlice(increase, multiplier);
	}
	public double getIncrease() {
		return increase;
	}
	public double getMultiplier() {
		return multiplier;
	}
	public void addIncrease(double amount) {
		increase += amount;
	}
	public void addMultiplier(double amount) {
		multiplier += amount;
	}
	public boolean isEmpty() {
		return increase == 0 && multiplier == 0;
	}
}
