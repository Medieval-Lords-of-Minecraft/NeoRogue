package me.neoblade298.neorogue.player;

public interface TriggerAction {
	// If false, remove the trigger
	public boolean trigger(Object[] inputs);
}
