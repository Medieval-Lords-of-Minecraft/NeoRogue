package me.neoblade298.neorogue.session.fight.trigger.event;

import me.neoblade298.neorogue.session.fight.DamageMeta;

public class EvadeEvent {
	private double evadeMitigated, startingDamage;
	private DamageMeta dm;
	public EvadeEvent(double startingDamage, double evadeMitigated, DamageMeta dm) {
		this.evadeMitigated = evadeMitigated;
		this.startingDamage = startingDamage;
		this.dm = dm;
	}

	public double getEvadeMitigated() {
		return evadeMitigated;
	}
	public double getStartingDamage() {
		return startingDamage;
	}
	public boolean isFullyEvaded() {
		return evadeMitigated >= startingDamage;
	}
	public DamageMeta getDamageMeta() {
		return dm;
	}
}
