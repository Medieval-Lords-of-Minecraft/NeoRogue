package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;

public class ConcussedStatus extends FixedContributionStatus {
	private static final double PHYSICAL_DAMAGE_DEBUFF = -0.25;
	
	public ConcussedStatus(FightData data) {
		super(StatusType.CONCUSSED, data, DamageCategory.PHYSICAL, 0, PHYSICAL_DAMAGE_DEBUFF, false, false);
	}
}
