package me.neoblade298.neorogue.session.event;

import java.util.ArrayList;

import me.neoblade298.neorogue.session.reward.Reward;

public class ClearRewardsEvent {
	private ArrayList<Reward> rewards;
	public ClearRewardsEvent(ArrayList<Reward> rewards) {
		this.rewards = rewards;
	}

	public ArrayList<Reward> getRewards() {
		return rewards;
	}
}
