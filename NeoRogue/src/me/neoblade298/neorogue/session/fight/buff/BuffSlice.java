package me.neoblade298.neorogue.session.fight.buff;

public class BuffSlice {
	private double increase, multiplier;
	
	public BuffSlice() {}
	public BuffSlice(double increase, double multiplier) {
		this.increase = increase;
		this.multiplier = multiplier;
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
