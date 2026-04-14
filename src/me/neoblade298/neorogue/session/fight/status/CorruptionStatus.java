package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;

public class CorruptionStatus extends BasicStatus {
	private static String id = "CORRUPTION";

	public CorruptionStatus(FightData data) {
		super(id, data, StatusClass.NEGATIVE);
	}

	@Override
	public void onApply(FightData applier, int stacks) {
		super.onApply(applier, stacks);
		holder.addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL),
				Buff.multiplier(applier, -stacks * 0.5, BuffStatTracker.of(StatusType.CORRUPTION)));
	}
}
