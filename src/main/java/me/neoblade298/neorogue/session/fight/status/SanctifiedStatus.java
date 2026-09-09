package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;

public class SanctifiedStatus extends FixedContributionStatus {
	private static final double LIGHT_DEFENSE_DEBUFF = -0.5;

	public SanctifiedStatus(FightData data) {
		super(StatusType.SANCTIFIED, data, DamageCategory.LIGHT, 0, LIGHT_DEFENSE_DEBUFF, true, true);
	}
}