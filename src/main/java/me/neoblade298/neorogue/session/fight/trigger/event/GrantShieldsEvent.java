package me.neoblade298.neorogue.session.fight.trigger.event;

import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.Shield;
import me.neoblade298.neorogue.session.fight.buff.BuffList;

public class GrantShieldsEvent {
	private FightData applier, target;
	private boolean isSecondary;
	private Shield shield;
	private BuffList durationBuff = new BuffList(), amountBuff = new BuffList();
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
	public BuffList getAmountBuff() {
		return amountBuff;
	}
	public BuffList getDurationBuff() {
		return durationBuff;
	}
}
