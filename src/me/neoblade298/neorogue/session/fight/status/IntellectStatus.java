package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;

public class IntellectStatus extends BasicStatus {
	public IntellectStatus(FightData target) {
		super("INTELLECT", target, StatusClass.POSITIVE);
	}

	@Override
	public void onApply(FightData applier, int stacks) {
		super.onApply(applier, stacks);
		holder.addDamageBuff(DamageBuffType.of(DamageCategory.MAGICAL), new Buff(applier, stacks, 0, StatTracker.of(StatusType.INTELLECT)));
	}
}
