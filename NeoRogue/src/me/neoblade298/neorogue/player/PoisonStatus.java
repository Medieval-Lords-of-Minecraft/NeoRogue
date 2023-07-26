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
	private static String id;
	private int seconds;
	private HashMap<UUID, Integer> slices = new HashMap<UUID, Integer>();

	public PoisonStatus(FightData data, UUID applier, int stacks, int seconds) {
		super(id, stacks, data);
		this.seconds = seconds;
		action = new PoisonTickAction();
		data.addTickAction(action);
		slices.put(applier, stacks);
	}

	private class PoisonTickAction implements TickAction {
		@Override
		public boolean run() {
			FightInstance.receiveDamage(null, DamageType.POISON, stacks, true, false, data.getEntity());
			for (Entry<UUID, Integer> ent : slices.entrySet()) {
				FightInstance.getFightData(ent.getKey()).getStats().addDamageDealt(DamageType.POISON, ent.getValue());
			}
			
			seconds--;
			if (seconds <= 0) {
				data.removeStatus(id);
				return true;
			}
			return false;
		}
	}
	
	public void apply(UUID applier, int stacks, int seconds) {
		slices.put(applier, slices.getOrDefault(applier, 0));
		this.stacks += stacks;
		this.seconds = Math.max(this.seconds, seconds);
	}
}
