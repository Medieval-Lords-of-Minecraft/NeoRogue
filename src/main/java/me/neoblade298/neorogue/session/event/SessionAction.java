package me.neoblade298.neorogue.session.event;

import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public interface SessionAction {
	// Return a TriggerResult to control the trigger, mirroring fight triggers. TriggerResult.keep()
	// leaves the trigger registered, TriggerResult.remove() unregisters it after this run, and
	// TriggerResult.consume() stops lower actions on the same event from firing.
	public TriggerResult trigger(PlayerSessionData data, Object inputs);
}
