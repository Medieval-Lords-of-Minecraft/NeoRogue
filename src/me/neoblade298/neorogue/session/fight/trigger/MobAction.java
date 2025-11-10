package me.neoblade298.neorogue.session.fight.trigger;

import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public interface MobAction {
	public TriggerResult trigger(PlayerFightData src, FightData data, Object in);
}
