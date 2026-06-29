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
	
	// Keep the trigger, don't cancel the event
	public static TriggerResult keep() {
		return responses.get(hashCode(false, false));
	}
	
	// Remove the trigger, don't cancel the event
	public static TriggerResult remove() {
		return responses.get(hashCode(true, false));
	}
	
	// Keep the trigger, cancel the event
	public static TriggerResult cancel() {
		return responses.get(hashCode(false, true));
	}
	
	// Remove the trigger and cancel the event
	public static TriggerResult removeAndCancel() {
		return responses.get(hashCode(true, true));
	}
	
	// Keep the trigger, cancel the event only if the condition is met
	public static TriggerResult cancelIf(boolean cancelEvent) {
		return responses.get(hashCode(false, cancelEvent));
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
