package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.TickAction;

public class DurationStatus extends Status {
	public DurationStatus(String id, FightData target) {
		super(id, target);
	}

	@Override
	public void apply(FightData applier, int stacks, int seconds) {
		this.seconds = seconds > 0 ? Math.max(this.seconds, seconds) : this.seconds + seconds;
		this.stacks += stacks;
		
		if (this.stacks > 0 && this.seconds > 0) {
			if (action == null) {
				action = new DurationTickAction();
				data.addTickAction(action);
			}
		}

		if (this.stacks <= 0 || this.seconds <= 0) {
			data.removeStatus(id);
			return;
		}

		if (stacks > 0) slices.add(applier, stacks);
		else if (stacks < 0) slices.remove(stacks);
	}

	private class DurationTickAction extends TickAction {
		@Override
		public TickResult run() {
			if (action.isCancelled()) return TickResult.REMOVE;
			if (seconds <= 0) return TickResult.REMOVE; // If seconds was set below 0 with an apply
			onRun();
			
			if (--seconds <= 0) {
				data.removeStatus(id);
				return TickResult.REMOVE;
			}
			return TickResult.KEEP;
		}
		
		public void onRun() {
			onTickAction();
		}
	}
	
	public void onTickAction() {}
}
