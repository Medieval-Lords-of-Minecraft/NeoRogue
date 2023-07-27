package me.neoblade298.neorogue.player;

import java.util.UUID;

import me.neoblade298.neorogue.session.fights.FightData;
import me.neoblade298.neorogue.session.fights.TickAction;

public abstract class Status {
	protected String id;
	protected int stacks;
	protected TickAction action;
	protected FightData data;
	
	public Status(String id, FightData data) {
		this.id = id;
		this.data = data;
	}
	
	// Setting stacks or status to 0 means they will be untouched
	public abstract void apply(UUID applier, int stacks, int seconds);
	
	public static Status createFromId(String id, UUID applier, FightData target, int stacks, int seconds) {
		switch (id) {
		case "POISON": return new PoisonStatus(target, seconds);
		case "BLEED": return new BleedStatus(target);
		default: return new GenericStatus(id, target, seconds);
		}
	}
	
	public void cleanup() {
		if (action != null) action.setCancelled(true);
	}
}
