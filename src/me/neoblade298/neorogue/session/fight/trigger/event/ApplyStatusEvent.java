package me.neoblade298.neorogue.session.fight.trigger.event;

import javax.annotation.Nullable;

import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.StatusClass;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;

public class ApplyStatusEvent {
	// FightData target, Status ID, stacks, duration
	private FightData target;
	private String statusId;
	private DamageMeta meta; // Useful for adding damage slices to sources that primarily damage while applying status (ex. BurningCross)
	private int stacks, ticks;
	private Status s;
	private boolean isSecondary;
	private StatusClass sc;
	public ApplyStatusEvent(FightData target, Status s, int stacks, int ticks, boolean isSecondary) {
		this.target = target;
		this.s = s;
		this.statusId = s.getId();
		this.stacks = stacks;
		this.ticks = ticks;
		this.sc = s.getStatusClass();
	}
	public ApplyStatusEvent(FightData target, Status s, int stacks, int ticks, boolean isSecondary, @Nullable DamageMeta meta) {
		this(target, s, stacks, ticks, isSecondary);
		this.meta = meta;
	}
	public boolean isSecondary() {
		return isSecondary;
	}
	public FightData getTarget() {
		return target;
	}
	public String getStatusId() {
		return statusId;
	}
	public DamageMeta getMeta() {
		return meta;
	}
	public StatusClass getStatusClass() {
		return sc;
	}
	// Should only ever be used if you need a copy of a status, like with Mana Haze
	public Status getStatus() {
		return s;
	}
	public int getStacks() {
		return stacks;
	}
	public int getTicks() {
		return ticks;
	}
	public boolean isStatus(StatusType type) {
		return statusId.equals(type.name());
	}
	public boolean isStatus(String id) {
		return statusId.equals(id);
	}
}
