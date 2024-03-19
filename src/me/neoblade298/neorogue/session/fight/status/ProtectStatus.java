package me.neoblade298.neorogue.session.fight.status;

import java.util.UUID;

import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;

public class ProtectStatus extends BasicStatus {
	public ProtectStatus(FightData target) {
		super("PROTECT", target);
	}
	
	@Override
	public void onApply(UUID applier, int stacks) {
		this.stacks += stacks;
		data.addBuff(applier, false, false, BuffType.PHYSICAL, stacks);
	}
}
