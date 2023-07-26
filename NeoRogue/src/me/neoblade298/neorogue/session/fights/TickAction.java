package me.neoblade298.neorogue.session.fights;

public abstract class TickAction {
	private boolean cancelled;
	public abstract boolean run();
	
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	public boolean isCancelled() {
		return this.cancelled;
	}
}