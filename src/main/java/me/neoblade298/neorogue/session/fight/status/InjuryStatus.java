package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;

public class InjuryStatus extends FixedContributionStatus {
	private static final double PHYSICAL_DEFENSE_DEBUFF = -0.5;
	
	public InjuryStatus(FightData data) {
		super(StatusType.INJURY, data, DamageCategory.PHYSICAL, 0, PHYSICAL_DEFENSE_DEBUFF, true, true);
	}
}
