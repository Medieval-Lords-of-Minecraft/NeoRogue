package me.neoblade298.neorogue.session.fight.trigger;

import me.neoblade298.neorogue.session.fight.FightData;

public interface MobAction {
	public TriggerResult trigger(FightData data, Object in);
}
