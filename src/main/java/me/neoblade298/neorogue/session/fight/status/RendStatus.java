package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.FightData;

public class RendStatus extends DecrementStackStatus {
	private static String id = "REND";

	public RendStatus(FightData data) {
		super(id, data, StatusClass.NEGATIVE);
	}
}
