package me.neoblade298.neorogue.player;

import me.neoblade298.neorogue.session.fights.PlayerFightData;

public interface TriggerAction {
	// If false, remove the trigger
	public boolean trigger(PlayerFightData data, Object[] inputs);
	
	// If false, cancel the event
	// Right now the only event where this matters is receiving damage
	public default boolean isCancelled() {
		return false;
	}
	
	public default void cancel() {
		
	}
}
