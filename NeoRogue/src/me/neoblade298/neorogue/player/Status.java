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
	
	public abstract void apply(UUID applier, int stacks, int seconds);
	
	public static Status createFromId(String id, UUID applier, FightData target, int stacks, int seconds) {
		Status status;
		switch (id) {
		case "POISON": status = new PoisonStatus(target, applier, stacks, seconds);
		break;
		case "BLEED": status = new BleedStatus(target, applier, stacks);
		break;
		default: status = new GenericStatus(id, target, applier, stacks, seconds);
		break;
		}
		
		status.apply(applier, stacks, seconds);
		return status;
	}
	
	public void cleanup() {
		if (action != null) action.setCancelled(true);
	}
}
