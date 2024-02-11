package me.neoblade298.neorogue.session.fight.trigger.event;

import me.neoblade298.neorogue.session.fight.DamageMeta;

public class DealtDamageEvent {
	private DamageMeta meta;
	public DamageMeta getMeta() {
		return meta;
	}
	public void setMeta(DamageMeta meta) {
		this.meta = meta;
	}
	public DealtDamageEvent(DamageMeta meta) {
		super();
		this.meta = meta;
	}
}
