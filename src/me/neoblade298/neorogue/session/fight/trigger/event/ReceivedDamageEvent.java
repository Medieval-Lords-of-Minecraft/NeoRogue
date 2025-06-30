package me.neoblade298.neorogue.session.fight.trigger.event;

import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.FightData;

public class ReceivedDamageEvent {
	private FightData damager;
	private DamageMeta meta;
	private boolean isNullified = false;
	public FightData getDamager() {
		return damager;
	}
	public DamageMeta getMeta() {
		return meta;
	}
	public void setMeta(DamageMeta meta) {
		this.meta = meta;
	}
	public ReceivedDamageEvent(FightData damager, DamageMeta meta) {
		super();
		this.damager = damager;
		this.meta = meta;
	}
	public boolean isNullified() {
		return isNullified;
	}
	public void setNullified(boolean isNullified) {
		this.isNullified = isNullified;
	}
}
