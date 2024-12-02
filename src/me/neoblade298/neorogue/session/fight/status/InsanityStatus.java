package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;

public class InsanityStatus extends DecrementStackStatus {
	private static String id = "INSANITY";
	
	public InsanityStatus(FightData data) {
		super(id, data, StatusClass.NEGATIVE);
	}
	
	@Override
	public void apply(FightData applier, int stacks, int seconds) {
		super.apply(applier, stacks, seconds);
		holder.addBuff(false, DamageBuffType.of(DamageCategory.MAGICAL), new Buff(applier, stacks, -stacks * 0.2));
	}
	
	@Override
	public void onTickAction() {
		holder.addBuff(false, DamageBuffType.of(DamageCategory.MAGICAL), new Buff(slices.first().getFightData(), stacks, stacks * 0.2));
	}
}
