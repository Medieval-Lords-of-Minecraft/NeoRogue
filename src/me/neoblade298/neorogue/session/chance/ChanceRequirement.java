package me.neoblade298.neorogue.session.chance;

import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;

public interface ChanceRequirement {
	public boolean check(Session s, ChanceInstance inst, PlayerSessionData data);
}
