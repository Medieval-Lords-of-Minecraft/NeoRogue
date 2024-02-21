package me.neoblade298.neorogue.session.event;

import me.neoblade298.neorogue.player.PlayerSessionData;

public interface SessionAction {
	public void trigger(PlayerSessionData data, Object inputs);
}
