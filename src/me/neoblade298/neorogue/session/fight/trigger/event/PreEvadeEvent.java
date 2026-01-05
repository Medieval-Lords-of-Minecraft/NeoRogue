package me.neoblade298.neorogue.session.fight.trigger.event;

import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.buff.BuffList;

public class PreEvadeEvent {
	private DamageMeta dm;
	private BuffList staminaCostBuff = new BuffList();
	public PreEvadeEvent(DamageMeta dm) {
		this.dm = dm;
	}
	public DamageMeta getDamageMeta() {
		return dm;
	}
	public BuffList getStaminaCostBuff() {
		return staminaCostBuff;
	}
}
