package me.neoblade298.neorogue.session.fight.trigger.event;

import me.neoblade298.neorogue.session.fight.Trap;
import me.neoblade298.neorogue.session.fight.buff.Buff;

public class LayTrapEvent {
	private Trap trap;
	private Buff buff = new Buff();
	public LayTrapEvent(Trap trap) {
		this.trap = trap;
	}
	public Trap getTrap() {
		return this.trap;
	}
	public Buff getDurationBuff() {
		return buff;
	}
}
