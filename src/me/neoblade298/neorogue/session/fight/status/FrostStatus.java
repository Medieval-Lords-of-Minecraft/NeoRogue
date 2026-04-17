package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;

public class FrostStatus extends DecrementStackStatus {
	private static String id = "FROST";
	private static final double MAGIC_DAMAGE_DEBUFF = -0.25;
	
	public FrostStatus(FightData data) {
		super(id, data, StatusClass.NEGATIVE);
	}
	
	@Override
	public void apply(FightData applier, int stacks, int seconds) {
		super.apply(applier, stacks, seconds);
		if (this.stacks <= 0) return;
		Buff b = new Buff(slices.first().getFightData(), MAGIC_DAMAGE_DEBUFF, 0, BuffStatTracker.of(StatusType.FROST));
		holder.addDamageBuff(DamageBuffType.of(DamageCategory.MAGICAL), b);
	}
	
	@Override
	public void onTickAction(int toRemove) {
		if (stacks - toRemove <= 0) {
			Buff b = new Buff(slices.first().getFightData(), 0, 0, BuffStatTracker.of(StatusType.FROST));
			holder.addDamageBuff(DamageBuffType.of(DamageCategory.MAGICAL), b);
		}
	}
}
