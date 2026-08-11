package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;

public class BurnStatus extends FixedContributionStatus {
	private static final double FIRE_DEFENSE_DEBUFF = -0.5;
	
	public BurnStatus(FightData data) {
		super(StatusType.BURN, data, DamageCategory.FIRE, 0, FIRE_DEFENSE_DEBUFF, true, true);
	}
}
