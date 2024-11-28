package me.neoblade298.neorogue.session.fight.buff;

import java.util.HashMap;
import java.util.Map.Entry;

import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.FightData;

public class Buff {
	private double increase, multiplier;
	private BuffOrigin origin;
	private HashMap<FightData, BuffSlice> slices = new HashMap<FightData, BuffSlice>();
	
	public Buff() {}
	
	public Buff(FightData applier, double increase, double multiplier) {
		this(applier, increase, multiplier, BuffOrigin.NORMAL);
	}
	
	public Buff(FightData applier, double increase, double multiplier, BuffOrigin origin) {
		this.increase = increase;
		this.multiplier = multiplier;
		this.origin = origin;
		slices.put(applier, new BuffSlice(increase, multiplier));
	}
	
	// Used in clone
	private Buff(double increase, double multiplier, BuffOrigin origin, HashMap<FightData, BuffSlice> slices) {
		this.increase = increase;
		this.multiplier = multiplier;
		this.slices = slices;
		this.origin = origin;
	}

	public boolean isSimilar(Buff other) {
		return origin == other.origin;
	}

	public Buff combineBuff(Buff other) {
		this.increase += other.increase;
		this.multiplier += other.multiplier;
		for (Entry<FightData, BuffSlice> ent : other.slices.entrySet()) {
			BuffSlice slice = slices.getOrDefault(ent.getKey(), new BuffSlice());
			slice.addIncrease(ent.getValue().getIncrease());
			slice.addMultiplier(ent.getValue().getMultiplier());
			if (!slice.isEmpty()) {
				slices.put(ent.getKey(), slice);
			}
			else {
				slices.remove(ent.getKey());
			}
		}
		return this;
	}
	
	public Buff clone() {
		HashMap<FightData, BuffSlice> newSlices = new HashMap<FightData, BuffSlice>();
		for (Entry<FightData, BuffSlice> ent : slices.entrySet()) {
			newSlices.put(ent.getKey(), ent.getValue().clone());
		}
		return new Buff(increase, multiplier, origin, newSlices);
	}
	
	public void addIncrease(FightData applier, double increase) {
		if (increase == 0) return;
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
	
	public void addMultiplier(FightData applier, double getMultiplier) {
		if (multiplier == 0) return;
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
	
	public HashMap<FightData, BuffSlice> getSlices() {
		return slices;
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
