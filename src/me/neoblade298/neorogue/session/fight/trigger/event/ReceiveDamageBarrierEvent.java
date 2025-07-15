package me.neoblade298.neorogue.session.fight.trigger.event;

import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.FightData;

public class ReceiveDamageBarrierEvent extends ReceiveDamageEvent {
	private Barrier b;
	public ReceiveDamageBarrierEvent(FightData damager, DamageMeta meta, Barrier b) {
		super(damager, meta);
		this.b = b;
	}

	public Barrier getBarrier() {
		return b;
	}
}
