package me.neoblade298.neorogue.session.fight;

import me.neoblade298.neorogue.session.fight.status.Status;

public class StatusDamageSlice extends DamageSlice {
	public StatusDamageSlice(DamageType type, Status s, DamageStatTracker tracker) {
		super(s.getSlices().first().getFightData(), s.getStacks() * 0.2, type, tracker);
	}
}
