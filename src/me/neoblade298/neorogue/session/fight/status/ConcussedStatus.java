package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;

public class ConcussedStatus extends DecrementStackStatus {
	private static String id = "CONCUSSED";
	private static final double PHYSICAL_DAMAGE_DEBUFF = -0.25;
	
	public ConcussedStatus(FightData data) {
		super(id, data, StatusClass.NEGATIVE);
	}
	
	@Override
	public void apply(FightData applier, int stacks, int seconds) {
		super.apply(applier, stacks, seconds);
		if (this.stacks <= 0) return;
		Buff b = new Buff(slices.first().getFightData(), PHYSICAL_DAMAGE_DEBUFF, 0, BuffStatTracker.of(StatusType.CONCUSSED));
		holder.addDamageBuff(DamageBuffType.of(DamageCategory.PHYSICAL), b);
	}
	
	@Override
	public void onTickAction(int toRemove) {
		if (stacks - toRemove <= 0) {
			Buff b = new Buff(slices.first().getFightData(), 0, 0, BuffStatTracker.of(StatusType.CONCUSSED));
			holder.addDamageBuff(DamageBuffType.of(DamageCategory.PHYSICAL), b);
		}
	}
}
