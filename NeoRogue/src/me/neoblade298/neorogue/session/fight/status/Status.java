package me.neoblade298.neorogue.session.fight.status;

import java.util.UUID;

import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.TickAction;

public abstract class Status {
	protected String id;
	protected int stacks;
	protected TickAction action;
	protected FightData data;
	protected StatusSliceHolder slices = new StatusSliceHolder();
	protected int seconds;
	
	public Status(String id, FightData data) {
		this.id = id;
		this.data = data;
	}
	
	// Setting stacks or status to 0 means they will be untouched
	public abstract void apply(UUID applier, int stacks, int seconds);
	
	public static Status createByType(StatusType id, UUID applier, FightData target) {
		switch (id) {
		case POISON: return new PoisonStatus(target);
		case BLEED: return new BleedStatus(target);
		case BURN: return new DecrementStackStatus(id.name(), target);
		case FROST: return new DecrementStackStatus(id.name(), target);
		case ELECTRIFIED: return new DecrementStackStatus(id.name(), target);
		case CONCUSSED: return new ConcussedStatus(target);
		case INSANITY: return new DecrementStackStatus(id.name(), target);
		case SANCTIFIED: return new DecrementStackStatus(id.name(), target);
		case THORNS: return new BasicStatus(id.name(), target);
		default: return null;
		}
	}
	
	public static Status createByGenericType(GenericStatusType type, String id, UUID applier, FightData target) {
		switch (type) {
		case DECREMENT_STACK: return new DecrementStackStatus(id, target);
		case BASIC: return new BasicStatus(id, target);
		case DURATION: return new DurationStatus(id, target);
		default: return null;
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
	
	public enum StatusType {
		POISON, BLEED, BURN, FROST, ELECTRIFIED, CONCUSSED, INSANITY, SANCTIFIED, THORNS;
	}
	public enum GenericStatusType {
		DECREMENT_STACK, BASIC, DURATION;
	}
}
