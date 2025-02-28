package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;

public class InsanityStatus extends DecrementStackStatus {
	private static String id = "INSANITY";
	
	public InsanityStatus(FightData data) {
		super(id, data, StatusClass.NEGATIVE);
	}
	
	@Override
	public void apply(FightData applier, int stacks, int seconds) {
		super.apply(applier, stacks, seconds);
		holder.addDefenseBuff(DamageBuffType.of(DamageCategory.MAGICAL), Buff.increase(applier, -stacks * 0.2, BuffStatTracker.of(StatusType.INSANITY)));
	}
	
	@Override
	public void onTickAction() {
		holder.addDefenseBuff(DamageBuffType.of(DamageCategory.MAGICAL), Buff.increase(slices.first().getFightData(), stacks * 0.2, BuffStatTracker.of(StatusType.INSANITY)));
	}
}
