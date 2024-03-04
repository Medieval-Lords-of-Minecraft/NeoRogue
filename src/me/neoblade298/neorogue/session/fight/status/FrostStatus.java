package me.neoblade298.neorogue.session.fight.status;

import java.util.UUID;

import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;

public class FrostStatus extends DecrementStackStatus {
	private static String id = "FROST";
	
	public FrostStatus(FightData data) {
		super(id, data);
	}
	
	@Override
	public void apply(UUID applier, int stacks, int seconds) {
		super.apply(applier, stacks, seconds);
		data.addBuff(applier, true, false, BuffType.MAGICAL, -stacks * 0.2);
	}
	
	@Override
	public void onTickAction() {
		data.addBuff(slices.first().getUniqueId(), true, false, BuffType.MAGICAL, 0.2);
	}
}
