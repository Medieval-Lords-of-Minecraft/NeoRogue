package me.neoblade298.neorogue.player;

import java.util.UUID;

import me.neoblade298.neorogue.session.fights.FightData;
import me.neoblade298.neorogue.session.fights.FightInstance;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class BasicStatus extends Status {
	public BasicStatus(String id, FightData target) {
		super(id, target);
	}

	@Override
	public void apply(UUID applier, int stacks, int seconds) {
		this.stacks += stacks;
		if (FightInstance.getFightData(applier) instanceof PlayerFightData) slices.add(applier, stacks);
	}
}
