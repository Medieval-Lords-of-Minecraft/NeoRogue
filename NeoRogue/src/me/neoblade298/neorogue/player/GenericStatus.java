package me.neoblade298.neorogue.player;

import java.util.UUID;
import java.util.Map.Entry;

import me.neoblade298.neorogue.session.fights.DamageType;
import me.neoblade298.neorogue.session.fights.FightData;
import me.neoblade298.neorogue.session.fights.FightInstance;
import me.neoblade298.neorogue.session.fights.TickAction;

public class GenericStatus extends Status {
	private int seconds;

	public GenericStatus(String id, FightData target, UUID applier, int stacks, int seconds) {
		super(id, stacks, target);
		this.seconds = seconds;
		action = new GenericTickAction();
	}

	@Override
	public void apply(UUID applier, int stacks, int seconds) {
		this.stacks = stacks;
		this.seconds = seconds >= 0 ? seconds : -1;
	}


	private class GenericTickAction implements TickAction {
		@Override
		public boolean run() {
			if (seconds <= 0) return true; // If seconds was set below 0 with an apply
			
			if (--seconds <= 0) {
				data.removeStatus(id);
				return true;
			}
			return false;
		}
	}
}
