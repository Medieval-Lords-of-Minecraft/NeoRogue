package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;

public class FrostStatus extends DecrementStackStatus {
	private static String id = "FROST";
	
	public FrostStatus(FightData data) {
		super(id, data, StatusClass.NEGATIVE);
	}
	
	@Override
	public void apply(FightData applier, int stacks, int seconds) {
		super.apply(applier, stacks, seconds);
		holder.addDamageBuff(DamageBuffType.of(DamageCategory.MAGICAL), new Buff(slices.first().getFightData(), -stacks * 0.2, 0, BuffStatTracker.of(StatusType.FROST)));
	}
	
	@Override
	public void onTickAction(int toRemove) {
		holder.addDamageBuff(DamageBuffType.of(DamageCategory.MAGICAL), new Buff(slices.first().getFightData(), toRemove * 0.2, 0, BuffStatTracker.of(StatusType.FROST)));
	}
}
