package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;

public class ShellStatus extends BasicStatus {
	public ShellStatus(FightData target) {
		super("SHELL", target);
	}
	
	@Override
	public void onApply(FightData applier, int stacks) {
		this.stacks += stacks;
		data.addBuff(applier, false, false, BuffType.MAGICAL, stacks);
	}
}
