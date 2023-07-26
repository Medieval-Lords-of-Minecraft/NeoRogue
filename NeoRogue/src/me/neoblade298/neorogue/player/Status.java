package me.neoblade298.neorogue.player;

import org.bukkit.scheduler.BukkitTask;

import me.neoblade298.neorogue.session.fights.FightData;
import me.neoblade298.neorogue.session.fights.TickAction;

public abstract class Status {
	private String id;
	protected int stacks;
	protected TickAction action;
	protected FightData data;
	
	public Status(String id, int stacks, FightData data) {
		this.id = id;
		this.stacks = stacks;
		this.data = data;
	}
}
