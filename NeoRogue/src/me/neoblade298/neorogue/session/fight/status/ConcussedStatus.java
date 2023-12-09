package me.neoblade298.neorogue.session.fight.status;

import java.util.UUID;

import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;

public class ConcussedStatus extends DecrementStackStatus {
	private static String id = "CONCUSSED";
	
	public ConcussedStatus(FightData data) {
		super(id, data);
	}
	
	@Override
	public void apply(UUID applier, int stacks, int seconds) {
		super.apply(applier, stacks, seconds);
		data.addBuff(applier, true, false, BuffType.PHYSICAL, -1);
	}
	
	@Override
	public void onTickAction() {
		data.addBuff(slices.first().getUniqueId(), true, false, BuffType.PHYSICAL, 1);
	}
}
