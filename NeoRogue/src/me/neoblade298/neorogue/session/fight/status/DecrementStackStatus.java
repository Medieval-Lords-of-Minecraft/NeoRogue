package me.neoblade298.neorogue.session.fight.status;

import java.util.UUID;

import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TickAction;

public class DecrementStackStatus extends Status {

	public DecrementStackStatus(String id, FightData target) {
		super(id, target);
	}

	@Override
	public void apply(UUID applier, int stacks, int seconds) {
		this.stacks += stacks;
		
		if (this.stacks > 0) {
			if (action == null) {
				action = new DecrementStackTickAction();
				data.addTickAction(action);
			}
		}
		else {
			data.removeStatus(id);
			return;
		}

		if (FightInstance.getFightData(applier) instanceof PlayerFightData) slices.add(applier, stacks);
	}
	
	private class DecrementStackTickAction extends TickAction {
		@Override
		public TickResult run() {
			if (action.isCancelled()) return TickResult.REMOVE;
			onTickAction();
			
			stacks--;
			if (stacks <= 0) {
				data.removeStatus(id);
				return TickResult.REMOVE;
			}
			
			slices.remove(1);
			return TickResult.KEEP;
		}
	}
	
	public void onTickAction() {}
}
