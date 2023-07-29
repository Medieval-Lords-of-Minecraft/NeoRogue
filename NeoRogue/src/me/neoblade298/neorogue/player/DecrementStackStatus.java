package me.neoblade298.neorogue.player;

import java.util.UUID;

import me.neoblade298.neorogue.session.fights.FightData;
import me.neoblade298.neorogue.session.fights.FightInstance;
import me.neoblade298.neorogue.session.fights.PlayerFightData;
import me.neoblade298.neorogue.session.fights.TickAction;

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
		public boolean run() {
			if (action.isCancelled()) return true;
			onRun();
			
			stacks--;
			if (stacks <= 0) {
				data.removeStatus(id);
				return true;
			}
			
			slices.remove(1);
			return false;
		}
		
		public void onRun() {
			onTickAction();
		}
	}
	
	public void onTickAction() {}
}
