package me.neoblade298.neorogue.session.fight.trigger.event;

import javax.annotation.Nullable;

import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.FightData;

public class ApplyStatusEvent {
	// FightData target, Status ID, stacks, duration
	private FightData target;
	private String statusId;
	private int stacks, duration;
	private DamageMeta meta;
	public ApplyStatusEvent(FightData target, String statusId, int stacks, int duration) {
		this.target = target;
		this.statusId = statusId;
		this.stacks = stacks;
		this.duration = duration;
	}
	public ApplyStatusEvent(FightData target, String statusId, int stacks, int duration, @Nullable DamageMeta meta) {
		this.target = target;
		this.statusId = statusId;
		this.stacks = stacks;
		this.duration = duration;
		this.meta = meta;
	}
	public FightData getTarget() {
		return target;
	}
	public void setTarget(FightData target) {
		this.target = target;
	}
	public String getStatusId() {
		return statusId;
	}
	public void setStatusId(String statusId) {
		this.statusId = statusId;
	}
	public int getStacks() {
		return stacks;
	}
	public void setStacks(int stacks) {
		this.stacks = stacks;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public DamageMeta getMeta() {
		return meta;
	}
	public void setMeta(DamageMeta meta) {
		this.meta = meta;
	}
}
