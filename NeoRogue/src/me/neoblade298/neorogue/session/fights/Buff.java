package me.neoblade298.neorogue.session.fights;

import java.util.HashMap;
import java.util.UUID;

public class Buff {
	private double increase, multiplier;
	private HashMap<UUID, BuffSlice> slices = new HashMap<UUID, BuffSlice>();
	
	public Buff(UUID applier) {
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
	
	public void addIncrease(UUID applier, double increase) {
		this.increase += increase;
		BuffSlice slice = slices.getOrDefault(applier, new BuffSlice());
		slice.addIncrease(increase);
		
		if (slice.isEmpty()) {
			slices.put(applier, slice);
		}
		else {
			slices.remove(applier);
		}
	}
	
	public void addMultiplier(UUID applier, double multiplier) {
		this.multiplier += multiplier;
		BuffSlice slice = slices.getOrDefault(applier, new BuffSlice());
		slice.addMultiplier(multiplier);
		
		if (slice.isEmpty()) {
			slices.put(applier, slice);
		}
		else {
			slices.remove(applier);
		}
	}
	
	public HashMap<UUID, BuffSlice> getSlices() {
		return slices;
	}
	
	public double getIncrease() {
		return increase;
	}
	
	public double getMultiplier() {
		return multiplier;
	}
}
