package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;

public class StrengthStatus extends BasicStatus {
	public StrengthStatus(FightData target) {
		super("STRENGTH", target);
	}

	@Override
	public void onApply(FightData applier, int stacks) {
		super.onApply(applier, stacks);
		data.addBuff(applier, true, false, BuffType.PHYSICAL, stacks);
	}
}
