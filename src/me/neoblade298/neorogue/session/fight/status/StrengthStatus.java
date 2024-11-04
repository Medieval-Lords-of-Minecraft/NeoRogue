package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;

public class StrengthStatus extends BasicStatus {
	public StrengthStatus(FightData target) {
		super("STRENGTH", target, StatusClass.POSITIVE);
	}

	@Override
	public void onApply(FightData applier, int stacks) {
		super.onApply(applier, stacks);
		holder.addBuff(applier, true, false, BuffType.PHYSICAL, stacks);
	}
}
