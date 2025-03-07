package me.neoblade298.neorogue.session.fight.trigger.event;

import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.FightData;

public class ReceivedDamageBarrierEvent extends ReceivedDamageEvent {
	private Barrier b;
	public ReceivedDamageBarrierEvent(FightData damager, DamageMeta meta, Barrier b) {
		super(damager, meta);
		this.b = b;
	}

	public Barrier getBarrier() {
		return b;
	}
}
