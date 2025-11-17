package me.neoblade298.neorogue.session.fight.trigger.event;

import javax.annotation.Nullable;

import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.buff.BuffList;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.StatusClass;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;

public class PreApplyStatusEvent {
	// FightData target, Status ID, stacks, duration
	private FightData target;
	private String statusId;
	private DamageMeta meta;
	private int stacks, ticks;
	private BuffList stackBuff = new BuffList(), durationBuff = new BuffList();
	private boolean isSecondary;
	private StatusClass sc;
	private Status s;
	public PreApplyStatusEvent(FightData target, Status s, int stacks, int ticks, boolean isSecondary) {
		this.target = target;
		this.statusId = s.getId();
		this.stacks = stacks;
		this.ticks = ticks;
		this.sc = s.getStatusClass();
		this.s = s;
		this.isSecondary = isSecondary;
	}
	public PreApplyStatusEvent(FightData target, Status s, int stacks, int ticks, boolean isSecondary, @Nullable DamageMeta meta) {
		this(target, s, stacks, ticks, isSecondary);
		this.meta = meta;
	}
	public FightData getTarget() {
		return target;
	}
	// Should only ever be used if you need a copy of a status, like with Mana Haze
	public Status getStatus() {
		return s;
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
	public BuffList getStacksBuffList() {
		return stackBuff;
	}
	public BuffList getDurationBuffList() {
		return durationBuff;
	}
	public StatusClass getStatusClass() {
		return sc;
	}

	public boolean isSecondary() {
		return isSecondary;
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
