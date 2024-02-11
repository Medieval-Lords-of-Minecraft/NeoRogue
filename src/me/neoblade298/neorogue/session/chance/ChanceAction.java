package me.neoblade298.neorogue.session.chance;

import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;

public interface ChanceAction {
	// returns the next stage id (so that you can create the stages in order and reference them later)
	public String run(Session s, ChanceInstance inst, PlayerSessionData data);
}
