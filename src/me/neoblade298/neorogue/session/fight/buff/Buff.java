package me.neoblade298.neorogue.session.fight.buff;

import java.util.HashMap;
import java.util.Map.Entry;

import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.FightData;

public class Buff {
	private double increase, multiplier;
	private DamageOrigin origin;
	private HashMap<FightData, BuffSlice> slices = new HashMap<FightData, BuffSlice>();
	
	public Buff() {}
	
	public Buff(FightData applier, double increase, double multiplier) {
		this.increase = increase;
		this.multiplier = multiplier;
		slices.put(applier, new BuffSlice(increase, multiplier));
	}
	
	public Buff(FightData applier, double increase, double multiplier, DamageOrigin origin) {
		this.increase = increase;
		this.multiplier = multiplier;
		this.origin = origin;
		slices.put(applier, new BuffSlice(increase, multiplier));
	}
	
	private Buff(double increase, double multiplier, DamageOrigin origin, HashMap<FightData, BuffSlice> slices) {
		this.increase = increase;
		this.multiplier = multiplier;
		this.slices = slices;
	}
	
	public Buff clone() {
		HashMap<FightData, BuffSlice> newSlices = new HashMap<FightData, BuffSlice>();
		for (Entry<FightData, BuffSlice> ent : slices.entrySet()) {
			newSlices.put(ent.getKey(), ent.getValue().clone());
		}
		return new Buff(increase, multiplier, origin, newSlices);
	}

	public DamageOrigin getOrigin() {
		return origin;
	}

	public void setOrigin(DamageOrigin origin) {
		this.origin = origin;
	}
	
	public void addIncrease(FightData applier, double increase) {
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
	
	public void addMultiplier(FightData applier, double multiplier) {
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
