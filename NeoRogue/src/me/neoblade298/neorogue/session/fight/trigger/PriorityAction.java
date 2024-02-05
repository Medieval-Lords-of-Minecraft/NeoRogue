package me.neoblade298.neorogue.session.fight.trigger;

import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class PriorityAction implements TriggerAction, Comparable<PriorityAction> {
	protected String id;
	protected int priority = 0;
	protected TriggerAction action;
	public PriorityAction(String id) {
		this.id = id;
	}
	public PriorityAction(String id, TriggerAction action) {
		this.id = id;
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
		int comp = Integer.compare(priority, o.priority);
		if (comp != 0) return comp;
		return id.compareTo(o.id);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + priority;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PriorityAction other = (PriorityAction) obj;
		if (action == null) {
			if (other.action != null) return false;
		}
		else if (!action.equals(other.action)) return false;
		if (id == null) {
			if (other.id != null) return false;
		}
		else if (!id.equals(other.id)) return false;
		if (priority != other.priority) return false;
		return true;
	}
	@Override
	public String toString() {
		return id;
	}
}
