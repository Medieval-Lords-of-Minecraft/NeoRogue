package me.neoblade298.neorogue.session.fight.trigger.event;

import javax.annotation.Nullable;

import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;

public class ApplyStatusEvent {
	// FightData target, Status ID, stacks, duration
	private FightData target;
	private String statusId;
	private DamageMeta meta;
	private int stacks, seconds;
	private Buff stackBuff = new Buff(), durationBuff = new Buff();
	public ApplyStatusEvent(FightData target, String statusId, int stacks, int duration) {
		this.target = target;
		this.statusId = statusId;
		this.stacks = stacks;
		this.seconds = duration;
	}
	public ApplyStatusEvent(FightData target, String statusId, int stacks, int duration, @Nullable DamageMeta meta) {
		this(target, statusId, stacks, duration);
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
	public DamageMeta getMeta() {
		return meta;
	}
	public Buff getStacksBuff() {
		return stackBuff;
	}
	public Buff getDurationBuff() {
		return durationBuff;
	}
	public int getStacks() {
		return stacks;
	}
	public int getSeconds() {
		return seconds;
	}
}
