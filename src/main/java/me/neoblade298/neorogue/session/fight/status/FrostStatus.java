package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;

public class FrostStatus extends FixedContributionStatus {
	private static final double MAGIC_DAMAGE_DEBUFF = -0.25;
	
	public FrostStatus(FightData data) {
		super(StatusType.FROST, data, DamageCategory.MAGICAL, MAGIC_DAMAGE_DEBUFF, 0, false, false);
	}
}
