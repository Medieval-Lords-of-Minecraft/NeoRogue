package me.neoblade298.neorogue.player;

public interface TriggerAction {
	// If false, remove the trigger
	public boolean trigger(Object[] inputs);
	
	// If false, cancel the event
	// Right now the only event where this matters is receiving damage
	public default boolean isCancelled() {
		return false;
	}
}
