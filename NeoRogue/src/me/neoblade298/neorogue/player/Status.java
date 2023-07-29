package me.neoblade298.neorogue.player;

import java.util.UUID;

import me.neoblade298.neorogue.session.fights.FightData;
import me.neoblade298.neorogue.session.fights.TickAction;

public abstract class Status {
	protected String id;
	protected int stacks;
	protected TickAction action;
	protected FightData data;
	protected StatusSliceHolder slices;
	protected int seconds;
	
	public Status(String id, FightData data) {
		this.id = id;
		this.data = data;
	}
	
	// Setting stacks or status to 0 means they will be untouched
	public abstract void apply(UUID applier, int stacks, int seconds);
	
	public static Status createFromId(String id, UUID applier, FightData target) {
		switch (id) {
		case "POISON": return new PoisonStatus(target);
		case "BLEED": return new BleedStatus(target);
		case "BURN": return new DecrementStackStatus(id, target);
		case "FROST": return new DecrementStackStatus(id, target);
		case "ELECTRIFIED": return new DecrementStackStatus(id, target);
		case "CONCUSSED": return new DecrementStackStatus(id, target);
		case "INSANITY": return new DecrementStackStatus(id, target);
		case "SANCTIFIED": return new DecrementStackStatus(id, target);
		default: return new DurationStatus(id, target);
		}
	}
	
	public void cleanup() {
		if (action != null) action.setCancelled(true);
	}
	
	public StatusSliceHolder getSlices() {
		return slices;
	}
	
	public int getStacks() {
		return stacks;
	}
}
