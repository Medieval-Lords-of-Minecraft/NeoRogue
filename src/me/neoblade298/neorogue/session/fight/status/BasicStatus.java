package me.neoblade298.neorogue.session.fight.status;

import java.util.UUID;

import me.neoblade298.neorogue.session.fight.FightData;

public class BasicStatus extends Status {
	public BasicStatus(String id, FightData target) {
		super(id, target);
	}

	@Override
	public void apply(UUID applier, int stacks, int seconds) {
		this.stacks += stacks;
		slices.add(applier, stacks);
	}
}
