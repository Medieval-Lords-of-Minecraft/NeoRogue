package me.neoblade298.neorogue.session.chance;

import me.neoblade298.neorogue.session.Session;

public interface ChanceAction {
	public boolean run(Session s, boolean run); // If false, only check conditions passing
}
