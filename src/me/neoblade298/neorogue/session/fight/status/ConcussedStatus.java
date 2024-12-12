package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;

public class ConcussedStatus extends DecrementStackStatus {
	private static String id = "CONCUSSED";
	
	public ConcussedStatus(FightData data) {
		super(id, data, StatusClass.NEGATIVE);
	}
	
	@Override
	public void apply(FightData fd, int stacks, int seconds) {
		super.apply(fd, stacks, seconds);
		holder.addDamageBuff(DamageBuffType.of(DamageCategory.PHYSICAL), new Buff(slices.first().getFightData(), -stacks * 0.2, 0, StatTracker.of(StatusType.CONCUSSED)));
	}
	
	@Override
	public void onTickAction() {
		holder.addDamageBuff(DamageBuffType.of(DamageCategory.PHYSICAL), new Buff(slices.first().getFightData(), -0.2, 0, StatTracker.of(StatusType.CONCUSSED)));
	}
}
