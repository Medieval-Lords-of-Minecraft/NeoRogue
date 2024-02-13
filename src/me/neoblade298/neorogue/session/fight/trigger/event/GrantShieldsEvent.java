package me.neoblade298.neorogue.session.fight.trigger.event;

import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.Shield;

public class GrantShieldsEvent {
	private FightData applier, target;
	private boolean isSecondary;
	private Shield shield;
	public GrantShieldsEvent(FightData applier, FightData target, Shield shield, boolean isSecondary) {
		this.applier = applier;
		this.target = target;
		this.shield = shield;
		this.isSecondary = isSecondary;
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
	public boolean isSecondary() {
		return this.isSecondary;
	}
}
