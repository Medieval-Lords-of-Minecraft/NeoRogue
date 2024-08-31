package me.neoblade298.neorogue.equipment;

import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;

// For most common use cases of having a counter and cooldown
public class StandardPriorityAction extends PriorityAction {
	public StandardPriorityAction(String id) {
		super(id);
	}

	public StandardPriorityAction(String id, TriggerAction action) {
		super(id, action);
	}
	private long nextUse, time;
	private int count;
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public void addCount(int count) {
		this.count += count;
	}
	
	public int getCount() {
		return count;
	}
	
	public void setNextUse(long next) {
		this.nextUse = next;
	}
	
	public boolean canUse() {
		return System.currentTimeMillis() >= nextUse;
	}

	public void setTime(long time) {
		this.time = time;
	}
	
	public long getTime() {
		return time;
	}
}
