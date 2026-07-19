package me.neoblade298.neorogue.session.fight.trigger;

import java.util.HashMap;

public class TriggerResult {
	private static final HashMap<Integer, TriggerResult> responses = new HashMap<Integer, TriggerResult>();
	
	private boolean removeTrigger, cancelEvent, breakLoop;
	
	static {
		boolean[] b = new boolean[] {false, true};
		for (boolean removeTrigger : b) {
			for (boolean cancelEvent : b) {
				for (boolean breakLoop : b) {
					TriggerResult tr = new TriggerResult(removeTrigger, cancelEvent, breakLoop);
					responses.put(tr.hashCode(), tr);
				}
			}
		}
	}
	
	// Keep the trigger, don't cancel the event
	public static TriggerResult keep() {
		return responses.get(hashCode(false, false, false));
	}
	
	// Remove the trigger, don't cancel the event
	public static TriggerResult remove() {
		return responses.get(hashCode(true, false, false));
	}
	
	// Keep the trigger, cancel the event
	public static TriggerResult cancel() {
		return responses.get(hashCode(false, true, false));
	}
	
	// Remove the trigger and cancel the event
	public static TriggerResult removeAndCancel() {
		return responses.get(hashCode(true, true, false));
	}
	
	// Keep the trigger, cancel the event only if the condition is met
	public static TriggerResult cancelIf(boolean cancelEvent) {
		return responses.get(hashCode(false, cancelEvent, false));
	}
	
	// Keep the trigger and don't cancel the event, but stop lower priority actions from triggering off the same event
	public static TriggerResult consume() {
		return responses.get(hashCode(false, false, true));
	}
	
	// Remove the trigger and stop lower priority actions from triggering off the same event
	public static TriggerResult removeAndConsume() {
		return responses.get(hashCode(true, false, true));
	}
	
	private TriggerResult(boolean removeTrigger, boolean cancelEvent, boolean breakLoop) {
		this.removeTrigger = removeTrigger;
		this.cancelEvent = cancelEvent;
		this.breakLoop = breakLoop;
	}
	
	public boolean removeTrigger() {
		return removeTrigger;
	}
	
	public boolean cancelEvent() {
		return cancelEvent;
	}
	
	public boolean breakLoop() {
		return breakLoop;
	}
	
	@Override
	public int hashCode() {
		return hashCode(removeTrigger, cancelEvent, breakLoop);
	}

	public static int hashCode(boolean removeTrigger, boolean cancelEvent, boolean breakLoop) {
		return (removeTrigger ? 4 : 0) + (cancelEvent ? 2 : 0) + (breakLoop ? 1 : 0);
	}
}
