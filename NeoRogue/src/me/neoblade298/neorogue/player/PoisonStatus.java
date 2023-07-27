package me.neoblade298.neorogue.player;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.UUID;

import me.neoblade298.neorogue.session.fights.DamageType;
import me.neoblade298.neorogue.session.fights.FightData;
import me.neoblade298.neorogue.session.fights.FightInstance;
import me.neoblade298.neorogue.session.fights.TickAction;

public class PoisonStatus extends Status {
	private static String id = "POISON";
	private int seconds;
	private HashMap<UUID, Integer> slices = new HashMap<UUID, Integer>();
	private LinkedList<StatusSlice> sliceOwners = new LinkedList<StatusSlice>();

	public PoisonStatus(FightData data, int seconds) {
		super(id, data);
		this.seconds = seconds;
	}

	private class PoisonTickAction extends TickAction {
		@Override
		public boolean run() {
			if (this.isCancelled()) return true;
			
			FightInstance.receiveDamage(null, DamageType.POISON, stacks, true, false, data.getEntity());
			for (Entry<UUID, Integer> ent : slices.entrySet()) {
				FightInstance.getFightData(ent.getKey()).getStats().addDamageDealt(DamageType.POISON, ent.getValue());
			}
			
			if (--seconds <= 0) {
				data.removeStatus(id);
				return true;
			}
			return false;
		}
	}
	
	public void apply(UUID applier, int stacks, int seconds) {
		if (seconds > 0) {
			this.seconds = Math.max(this.seconds, seconds);
			if (action == null) {
				action = new PoisonTickAction();
				data.addTickAction(action);
			}
		}
		else {
			this.seconds += seconds;
			if (this.seconds <= 0) {
				data.removeStatus(id);
				return;
			}
		}
		
		if (stacks > 0) {
			this.stacks += stacks;
			if (action == null) {
				action = new PoisonTickAction();
				data.addTickAction(action);
			}
			slices.put(applier, slices.getOrDefault(applier, 0));

			StatusSlice last = sliceOwners.peekLast();
			if (last != null && last.getUniqueId().equals(applier)) {
				last.addStacks(stacks);
			}
			else {
				sliceOwners.push(new StatusSlice(applier, stacks));
			}
		}
		else {
			this.stacks -= stacks;
			if (this.stacks <= 0) {
				data.removeStatus(id);
				return;
			}
			while (stacks > 0 || !sliceOwners.isEmpty()) {
				StatusSlice slice = sliceOwners.peek();
				if (slice.getStacks() > stacks) {
					slice.addStacks(-stacks);
					stacks = 0;
				}
				else {
					stacks -= slice.getStacks();
					sliceOwners.poll();
				}
			}
		}
		
	}
}
