package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;

public class StrengthStatus extends BasicStatus {
	public StrengthStatus(FightData target) {
		super("STRENGTH", target, StatusClass.POSITIVE);
	}

	@Override
	public void onApply(FightData applier, int stacks) {
		super.onApply(applier, stacks);
		holder.addDamageBuff(DamageBuffType.of(DamageCategory.PHYSICAL), new Buff(applier, stacks, 0, StatTracker.of(StatusType.STRENGTH)));
	}
}
