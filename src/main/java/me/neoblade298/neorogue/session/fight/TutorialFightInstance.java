package me.neoblade298.neorogue.session.fight;

import java.util.Set;
import java.util.UUID;

import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.Session;

public class TutorialFightInstance extends StandardFightInstance {

	public TutorialFightInstance(Session s, Set<UUID> players, RegionType type, int nodesVisited) {
		super(s, players, type, nodesVisited);
	}

	public TutorialFightInstance(Session s, Set<UUID> players, Map map) {
		super(s, players, map);
	}

	@Override
	protected void setupInstance(Session s) {
		super.setupInstance(s);
		scoreRequired = Math.ceil(scoreRequired / 2);
	}

	@Override
	protected double getInitialSpawnBudget() {
		return 2;
	}
}
