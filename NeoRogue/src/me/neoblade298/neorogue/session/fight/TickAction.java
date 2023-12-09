package me.neoblade298.neorogue.session.fight;

public abstract class TickAction {
	private boolean cancelled;
	public abstract TickResult run();
	
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	public boolean isCancelled() {
		return this.cancelled;
	}
	
	public enum TickResult {
		KEEP, REMOVE;
	}
}