package me.neoblade298.neorogue.session.chance;

import me.neoblade298.neorogue.session.Session;

public class TutorialChanceInstance extends ChanceInstance {

	public TutorialChanceInstance(Session s) {
		super(s);
	}

	public TutorialChanceInstance(Session s, String setId) {
		super(s, setId);
	}

	public TutorialChanceInstance(Session s, ChanceSet set) {
		super(s, set);
	}

	@Override
	public void setup() {
		super.setup();
	}
}
