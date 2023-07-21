package me.neoblade298.neorogue.session.fights;

import org.bukkit.scheduler.BukkitTask;

public class Buff {
	private double increase, multiplier;
	
	public Buff() {
		this.increase = 0;
		this.multiplier = 0;
	}
	
	public Buff(double increase) {
		this.increase = increase;
		this.multiplier = 0;
	}
	
	public Buff(double increase, double multiplier) {
		this.increase = increase;
		this.multiplier = multiplier;
	}
	
	public void addIncrease(double increase) {
		this.increase += increase;
	}
	
	public void addMultiplier(double multiplier) {
		this.multiplier += multiplier;
	}
	
	public double getIncrease() {
		return increase;
	}
	
	public double getMultiplier() {
		return multiplier;
	}
}
