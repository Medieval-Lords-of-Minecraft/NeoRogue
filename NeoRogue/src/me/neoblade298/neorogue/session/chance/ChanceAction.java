package me.neoblade298.neorogue.session.chance;

import me.neoblade298.neorogue.session.Session;

public interface ChanceAction {
	public boolean run(Session s, ChanceInstance inst, boolean run); // If run false, only check conditions passing
}
