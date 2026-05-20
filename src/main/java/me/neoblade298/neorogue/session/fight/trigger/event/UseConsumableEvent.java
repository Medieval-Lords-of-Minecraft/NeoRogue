package me.neoblade298.neorogue.session.fight.trigger.event;

import me.neoblade298.neorogue.equipment.Consumable;

public class UseConsumableEvent {
	private Consumable cons;

	public Consumable getConsumable() {
		return cons;
	}

	public UseConsumableEvent(Consumable cons) {
		super();
		this.cons = cons;
	}
}
