package me.neoblade298.neorogue.session.fight.trigger;

import me.neoblade298.neorogue.session.fight.PlayerFightData;

public interface TriggerAction {
	public TriggerResult trigger(PlayerFightData data, Object in);
}
