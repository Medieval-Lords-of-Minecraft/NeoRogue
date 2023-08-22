package me.neoblade298.neorogue.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import me.neoblade298.neorogue.session.fights.FightScore;

public class RewardsInstance implements Instance {
	HashMap<UUID, ArrayList<Reward>> rewards = new HashMap<UUID, ArrayList<Reward>>();
	
	public RewardsInstance(HashMap<UUID, ArrayList<Reward>> rewards) {
		this.rewards = rewards;
	}

	@Override
	public void start(Session s) {
		
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}
}
