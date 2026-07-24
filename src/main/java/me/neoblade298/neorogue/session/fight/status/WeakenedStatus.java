package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;

public class WeakenedStatus extends BasicStatus {
	private static String id = "WEAKENED";
	private static final double DAMAGE_DEBUFF = -0.5;
	
	public WeakenedStatus(FightData data) {
		super(id, data, StatusClass.NEGATIVE);
	}
	
	@Override
	public void apply(FightData applier, int stacks, int seconds) {
		super.apply(applier, stacks, seconds);
		Buff b = new Buff(slices.first().getFightData(), 0, this.stacks >= 0 ? DAMAGE_DEBUFF : 0, BuffStatTracker.of(StatusType.WEAKENED));
		holder.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), b);
	}
}
