package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;

public class ConcussedStatus extends DecrementStackStatus {
	private static String id = "CONCUSSED";
	
	public ConcussedStatus(FightData data) {
		super(id, data, StatusClass.NEGATIVE);
	}
	
	@Override
	public void apply(FightData fd, int stacks, int seconds) {
		super.apply(fd, stacks, seconds);
		data.addBuff(fd, true, false, BuffType.PHYSICAL, -stacks * 0.2);
	}
	
	@Override
	public void onTickAction() {
		data.addBuff(slices.first().getFightData(), true, false, BuffType.PHYSICAL, 0.2);
	}
}
