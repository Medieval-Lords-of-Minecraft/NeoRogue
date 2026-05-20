package me.neoblade298.neorogue.session.fight.trigger;

import java.util.HashMap;

public class TriggerResult {
	private static final HashMap<Integer, TriggerResult> responses = new HashMap<Integer, TriggerResult>();
	
	private boolean removeTrigger, cancelEvent;
	
	static {
		boolean[] b = new boolean[] {false, true};
		for (boolean removeTrigger : b) {
			for (boolean cancelEvent : b) {
				TriggerResult tr = new TriggerResult(removeTrigger, cancelEvent);
				responses.put(tr.hashCode(), tr);
			}
		}
	}
	
	public static TriggerResult keep() {
		return responses.get(hashCode(false, false));
	}
	
	public static TriggerResult remove() {
		return responses.get(hashCode(true, false));
	}
	
	public static TriggerResult of(boolean removeTrigger) {
		return responses.get(hashCode(removeTrigger, false));
	}
	
	public static TriggerResult of(boolean removeTrigger, boolean cancelEvent) {
		return responses.get(hashCode(removeTrigger, cancelEvent));
	}
	
	private TriggerResult(boolean removeTrigger, boolean cancelEvent) {
		this.removeTrigger = removeTrigger;
		this.cancelEvent = cancelEvent;
	}
	
	public boolean removeTrigger() {
		return removeTrigger;
	}
	
	public boolean cancelEvent() {
		return cancelEvent;
	}
	
	@Override
	public int hashCode() {
		return hashCode(removeTrigger, cancelEvent);
	}

	public static int hashCode(boolean removeTrigger, boolean cancelEvent) {
		return (removeTrigger ? 2 : 0) + (cancelEvent ? 1 : 0);
	}
}
