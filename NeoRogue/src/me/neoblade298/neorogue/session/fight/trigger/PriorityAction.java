package me.neoblade298.neorogue.session.fight.trigger;

import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class PriorityAction implements TriggerAction, Comparable<PriorityAction> {
	protected int priority = 0;
	protected TriggerAction action;
	public PriorityAction() {}
	public PriorityAction(TriggerAction action) {
		this.action = action;
	}
	@Override
	public TriggerResult trigger(PlayerFightData data, Object inputs) {
		return action.trigger(data, inputs);
	}
	public int getPriority() {
		return priority;
	}
	@Override
	public int compareTo(PriorityAction o) {
		return Integer.compare(priority, o.priority);
	}
}
