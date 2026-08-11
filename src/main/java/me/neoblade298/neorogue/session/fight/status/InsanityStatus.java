package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;

public class InsanityStatus extends FixedContributionStatus {
	private static final double MAGIC_DEFENSE_DEBUFF = -0.5;
	
	public InsanityStatus(FightData data) {
		super(StatusType.INSANITY, data, DamageCategory.MAGICAL, 0, MAGIC_DEFENSE_DEBUFF, true, true);
	}
}
