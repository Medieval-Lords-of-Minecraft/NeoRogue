package me.neoblade298.neorogue.session.fight.trigger.event;

import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.Shield;

public class GrantShieldsEvent {
	private FightData applier, target;
	private Shield shield;
	public GrantShieldsEvent(FightData applier, FightData target, Shield shield) {
		this.applier = applier;
		this.target = target;
		this.shield = shield;
	}
	public FightData getApplier() {
		return applier;
	}
	public FightData getTarget() {
		return target;
	}
	public Shield getShield() {
		return shield;
	}
}
