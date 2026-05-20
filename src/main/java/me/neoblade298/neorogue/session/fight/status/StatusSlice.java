package me.neoblade298.neorogue.session.fight.status;

import java.util.UUID;

import me.neoblade298.neorogue.session.fight.FightData;

public class StatusSlice {
	private FightData fd;
	private int stacks;
	public StatusSlice(FightData fd, int stacks) {
		this.fd = fd;
		this.stacks = stacks;
	}
	
	public void addStacks(int amount) {
		this.stacks += amount;
	}
	
	public int getStacks() {
		return stacks;
	}
	public FightData getFightData() {
		return fd;
	}
	
	public UUID getUniqueId() {
		return fd.getUniqueId();
	}
}
