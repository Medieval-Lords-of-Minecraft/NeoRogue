package me.neoblade298.neorogue.session.fight.trigger;

import org.bukkit.entity.Player;

import me.neoblade298.neorogue.session.fight.PlayerFightData;

public interface TriggerCondition {
	public boolean canTrigger(Player p, PlayerFightData data, Object event);
}
