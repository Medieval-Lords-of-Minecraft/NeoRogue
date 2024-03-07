package me.neoblade298.neorogue.session.fight.buff;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

public class Buff {
	private double increase, multiplier;
	private HashMap<UUID, BuffSlice> slices = new HashMap<UUID, BuffSlice>();
	
	public Buff() {}
	
	public Buff(UUID applier, double increase, double multiplier) {
		this.increase = increase;
		this.multiplier = multiplier;
		slices.put(applier, new BuffSlice(increase, multiplier));
	}
	
	public Buff(double increase, double multiplier, HashMap<UUID, BuffSlice> slices) {
		this.increase = increase;
		this.multiplier = multiplier;
		this.slices = slices;
	}
	
	public Buff clone() {
		HashMap<UUID, BuffSlice> newSlices = new HashMap<UUID, BuffSlice>();
		for (Entry<UUID, BuffSlice> ent : slices.entrySet()) {
			newSlices.put(ent.getKey(), ent.getValue().clone());
		}
		return new Buff(increase, multiplier, newSlices);
	}
	
	public void addIncrease(UUID applier, double increase) {
		this.increase += increase;
		BuffSlice slice = slices.getOrDefault(applier, new BuffSlice());
		slice.addIncrease(increase);
		
		if (!slice.isEmpty()) {
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
		
		if (!slice.isEmpty()) {
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
