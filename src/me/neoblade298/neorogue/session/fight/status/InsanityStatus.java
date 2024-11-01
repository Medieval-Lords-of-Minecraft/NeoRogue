package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;

public class InsanityStatus extends DecrementStackStatus {
	private static String id = "INSANITY";
	
	public InsanityStatus(FightData data) {
		super(id, data, StatusClass.NEGATIVE);
	}
	
	@Override
	public void apply(FightData applier, int stacks, int seconds) {
		super.apply(applier, stacks, seconds);
		owner.addBuff(applier, false, false, BuffType.MAGICAL, stacks * 0.2);
	}
	
	@Override
	public void onTickAction() {
		owner.addBuff(slices.first().getFightData(), true, false, BuffType.MAGICAL, -0.2);
	}
}
