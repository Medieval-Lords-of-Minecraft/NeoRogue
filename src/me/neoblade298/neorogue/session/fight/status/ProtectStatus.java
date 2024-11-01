package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;

public class ProtectStatus extends BasicStatus {
	public ProtectStatus(FightData target) {
		super("PROTECT", target, StatusClass.POSITIVE);
	}
	
	@Override
	public void onApply(FightData applier, int stacks) {
		this.stacks += stacks;
		owner.addBuff(applier, false, false, BuffType.PHYSICAL, stacks);
	}
}
