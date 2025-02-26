package me.neoblade298.neorogue.session.fight.trigger.event;

import me.neoblade298.neorogue.session.fight.Marker;
import me.neoblade298.neorogue.session.fight.Rift;
import me.neoblade298.neorogue.session.fight.buff.BuffList;

public class CreateRiftEvent {
	private Rift rift;
	private BuffList buff = new BuffList();
	public CreateRiftEvent(Rift rift) {
		this.rift = rift;
	}
	public Marker getRift() {
		return this.rift;
	}
	public BuffList getDurationBuffList() {
		return buff;
	}
}
