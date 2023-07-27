package me.neoblade298.neorogue.player;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.UUID;

import me.neoblade298.neorogue.session.fights.DamageType;
import me.neoblade298.neorogue.session.fights.FightData;
import me.neoblade298.neorogue.session.fights.FightInstance;
import me.neoblade298.neorogue.session.fights.TickAction;

public class BleedStatus extends Status {
	private static String id = "BLEED";
	private LinkedList<StatusSlice> slices = new LinkedList<StatusSlice>();
	private HashMap<UUID, Integer> sliceOwners = new HashMap<UUID, Integer>();

	public BleedStatus(FightData data) {
		super(id, data);
	}

	private class BleedTickAction extends TickAction {
		@Override
		public boolean run() {
			if (action.isCancelled()) return true;
			
			FightInstance.receiveDamage(null, DamageType.BLEED, stacks, true, false, data.getEntity());
			for (Entry<UUID, Integer> ent : sliceOwners.entrySet()) {
				FightInstance.getFightData(ent.getKey()).getStats().addDamageDealt(DamageType.BLEED, ent.getValue());
			}
			
			stacks--;
			if (stacks <= 0) {
				data.removeStatus(id);
				return true;
			}
			
			StatusSlice last = slices.peek();
			last.addStacks(-1);
			if (last.getStacks() == 0) {
				slices.removeFirst();
			}
			int amount = sliceOwners.get(last.getUniqueId()) - 1;
			if (amount == 0) {
				sliceOwners.remove(last.getUniqueId());
			}
			else {
				sliceOwners.put(last.getUniqueId(), amount);
			}
			return false;
		}
	}
	
	// Always gets called after a status is created
	public void apply(UUID applier, int stacks, int seconds) {
		this.stacks += stacks;
		
		if (this.stacks > 0) {
			if (action == null) {
				action = new BleedTickAction();
				data.addTickAction(action);
			}
		}
		else {
			data.removeStatus(id);
			return;
		}
		
		StatusSlice last = slices.peekLast();
		if (last != null && last.getUniqueId().equals(applier)) {
			last.addStacks(stacks);
		}
		else {
			slices.push(new StatusSlice(applier, stacks));
		}
		sliceOwners.put(applier, sliceOwners.getOrDefault(applier, 0) + stacks);
	}
}
