package me.neoblade298.neorogue.session.fight.trigger.event;

import javax.annotation.Nullable;

import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusClass;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;

public class ApplyStatusEvent {
	// FightData target, Status ID, stacks, duration
	private FightData target;
	private String statusId;
	private DamageMeta meta; // Useful for adding damage slices to sources that primarily damage while applying status (ex. BurningCross)
	private int stacks, seconds;
	private StatusClass sc;
	public ApplyStatusEvent(FightData target, String statusId, int stacks, int duration, StatusClass sc) {
		this.target = target;
		this.statusId = statusId;
		this.stacks = stacks;
		this.seconds = duration;
		this.sc = sc;
	}
	public ApplyStatusEvent(FightData target, String statusId, int stacks, int duration, StatusClass sc, @Nullable DamageMeta meta) {
		this(target, statusId, stacks, duration, sc);
		this.meta = meta;
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
	public int getStacks() {
		return stacks;
	}
	public int getSeconds() {
		return seconds;
	}
	public boolean isStatus(StatusType type) {
		return statusId.equals(type.name());
	}
	public boolean isStatus(String id) {
		return statusId.equals(id);
	}
}
