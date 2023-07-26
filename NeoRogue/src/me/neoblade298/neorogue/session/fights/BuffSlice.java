package me.neoblade298.neorogue.session.fights;

import java.util.UUID;

public class BuffSlice {
	private double increase, multiplier;
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
