package me.neoblade298.neorogue.session.fight.trigger.event;

import me.neoblade298.neorogue.session.fight.Trap;
import me.neoblade298.neorogue.session.fight.buff.BuffList;

public class LayTrapEvent {
	private Trap trap;
	private BuffList buff = new BuffList();
	public LayTrapEvent(Trap trap) {
		this.trap = trap;
	}
	public Trap getTrap() {
		return this.trap;
	}
	public BuffList getDurationBuffList() {
		return buff;
	}
}
