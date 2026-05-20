package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;

public class BurnStatus extends DecrementStackStatus {
	private static String id = "BURN";
	private static final double FIRE_DEFENSE_DEBUFF = -0.5;
	
	public BurnStatus(FightData data) {
		super(id, data, StatusClass.NEGATIVE);
	}
	
	@Override
	public void apply(FightData applier, int stacks, int seconds) {
		super.apply(applier, stacks, seconds);
		if (this.stacks <= 0) return;
		Buff b = new Buff(slices.first().getFightData(), 0, FIRE_DEFENSE_DEBUFF, BuffStatTracker.of(StatusType.BURN));
		holder.addDefenseBuff(DamageBuffType.of(DamageCategory.FIRE), b);
	}
	
	@Override
	public void onTickAction(int toRemove) {
		if (stacks - toRemove <= 0) {
			Buff b = new Buff(slices.first().getFightData(), 0, 0,
					BuffStatTracker.of(StatusType.BURN));
			holder.addDefenseBuff(DamageBuffType.of(DamageCategory.FIRE), b);
		}
	}
}
