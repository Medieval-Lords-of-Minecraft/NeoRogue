package me.neoblade298.neorogue.session.fight.status;

import java.util.UUID;

public class StatusSlice {
	private UUID uuid;
	private int stacks;
	public StatusSlice(UUID uuid, int stacks) {
		this.uuid = uuid;
		this.stacks = stacks;
	}
	
	public void addStacks(int amount) {
		this.stacks += amount;
	}
	
	public int getStacks() {
		return stacks;
	}
	
	public UUID getUniqueId() {
		return uuid;
	}
}
