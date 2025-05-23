package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;

public class BurnStatus extends DecrementStackStatus {
	private static String id = "BURN";
	
	public BurnStatus(FightData data) {
		super(id, data, StatusClass.NEGATIVE);
	}
	
	@Override
	public void apply(FightData applier, int stacks, int seconds) {
		super.apply(applier, stacks, seconds);
		holder.addDefenseBuff(DamageBuffType.of(DamageCategory.FIRE), new Buff(slices.first().getFightData(), -stacks * 0.2, 0, BuffStatTracker.of(StatusType.BURN)));
	}
	
	@Override
	public void onTickAction(int toRemove) {
		holder.addDefenseBuff(DamageBuffType.of(DamageCategory.FIRE), new Buff(slices.first().getFightData(), toRemove * 0.2, 0, BuffStatTracker.of(StatusType.BURN)));
	}
}
