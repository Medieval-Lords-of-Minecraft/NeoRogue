package me.neoblade298.neorogue.player;

import java.util.UUID;

import me.neoblade298.neorogue.session.fights.FightData;
import me.neoblade298.neorogue.session.fights.FightInstance;
import me.neoblade298.neorogue.session.fights.PlayerFightData;
import me.neoblade298.neorogue.session.fights.TickAction;

public class DurationStatus extends Status {
	public DurationStatus(String id, FightData target) {
		super(id, target);
	}

	@Override
	public void apply(UUID applier, int stacks, int seconds) {
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
		
		if (!(FightInstance.getFightData(applier) instanceof PlayerFightData)) return;
		if (stacks > 0) slices.add(applier, stacks);
		else if (stacks < 0) slices.remove(stacks);
	}

	private class DurationTickAction extends TickAction {
		@Override
		public boolean run() {
			if (action.isCancelled()) return true;
			if (seconds <= 0) return true; // If seconds was set below 0 with an apply
			onRun();
			
			if (--seconds <= 0) {
				data.removeStatus(id);
				return true;
			}
			return false;
		}
		
		public void onRun() {
			onTickAction();
		}
	}
	
	public void onTickAction() {}
}
