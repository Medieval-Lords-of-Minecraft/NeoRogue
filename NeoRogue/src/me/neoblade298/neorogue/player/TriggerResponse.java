package me.neoblade298.neorogue.player;

import java.util.HashMap;

public class TriggerResponse {
	private static final HashMap<Integer, TriggerResponse> responses = new HashMap<Integer, TriggerResponse>();
	
	private boolean removeTrigger, cancelEvent;
	
	static {
		boolean[] b = new boolean[] {false, true};
		for (boolean removeTrigger : b) {
			for (boolean cancelEvent : b) {
				TriggerResponse tr = new TriggerResponse(removeTrigger, cancelEvent);
				responses.put(tr.hashCode(), tr);
			}
		}
	}
	
	public static TriggerResponse of(boolean removeTrigger, boolean cancelEvent) {
		return responses.get(hashCode(removeTrigger, cancelEvent));
	}
	
	private TriggerResponse(boolean removeTrigger, boolean cancelEvent) {
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
