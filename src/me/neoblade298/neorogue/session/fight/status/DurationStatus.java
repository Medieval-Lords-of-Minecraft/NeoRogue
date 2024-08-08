package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.TickAction;

public class DurationStatus extends Status {
	public DurationStatus(String id, FightData target, StatusClass sc) {
		super(id, target, sc);
	}
	public DurationStatus(String id, FightData target, StatusClass sc, boolean hidden) {
		super(id, target, sc, hidden);
	}

	@Override
	public void apply(FightData applier, int stacks, int ticks) {
		this.ticks = ticks > 0 ? Math.max(this.ticks, ticks) : this.ticks + ticks;
		this.stacks += stacks;
		
		if (this.stacks > 0 && this.ticks > 0) {
			if (action == null) {
				action = new DurationTickAction();
				data.addTickAction(action);
			}
		}

		if (this.stacks <= 0 || this.ticks <= 0) {
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
			if (ticks <= 0) return TickResult.REMOVE; // If ticks was set below 0 with an apply
			onRun();
			
			ticks = ticks - 20;
			if (ticks <= 0) {
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
