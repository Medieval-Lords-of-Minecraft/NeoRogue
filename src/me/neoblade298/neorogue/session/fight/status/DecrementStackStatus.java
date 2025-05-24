package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.TickAction;

public class DecrementStackStatus extends Status {

	public DecrementStackStatus(String id, FightData target, StatusClass sc) {
		super(id, target, sc);
	}

	public DecrementStackStatus(String id, FightData target, StatusClass sc, boolean hidden) {
		super(id, target, sc, hidden);
	}

	@Override
	public void apply(FightData fd, int stacks, int seconds) {
		this.stacks += stacks;
		
		if (this.stacks > 0) {
			if (action == null) {
				action = new DecrementStackTickAction();
				holder.addTickAction(action);
			}
		}
		else {
			this.stacks = 0;
			holder.removeStatus(id);
			return;
		}

		slices.add(fd, stacks);
	}
	
	public class DecrementStackTickAction extends TickAction {
		protected boolean decrement = true;
		@Override
		public TickResult run() {
			if (action.isCancelled()) return TickResult.REMOVE;
			
			int toRemove = (int) Math.max(stacks * 0.2, 1);
			onTickAction(toRemove);
			if (!decrement) return TickResult.KEEP;
			stacks -= toRemove;
			if (stacks <= 0) {
				holder.removeStatus(id);
				return TickResult.REMOVE;
			}
			
			slices.remove(toRemove);
			return TickResult.KEEP;
		}
	}
	
	public void onTickAction(int stacksRemoved) {}
}
