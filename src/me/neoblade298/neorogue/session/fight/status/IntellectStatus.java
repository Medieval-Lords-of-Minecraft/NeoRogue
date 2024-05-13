package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;

public class IntellectStatus extends BasicStatus {
	public IntellectStatus(FightData target) {
		super("INTELLECT", target, StatusClass.POSITIVE);
	}

	@Override
	public void onApply(FightData applier, int stacks) {
		super.onApply(applier, stacks);
		data.addBuff(applier, true, false, BuffType.MAGICAL, stacks);
	}
}
