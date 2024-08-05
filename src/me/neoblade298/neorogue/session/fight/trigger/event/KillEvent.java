package me.neoblade298.neorogue.session.fight.trigger.event;

import org.bukkit.entity.LivingEntity;

public class KillEvent {
	private LivingEntity target;
	public KillEvent(LivingEntity target) {
		this.target = target;
	}
	public LivingEntity getTarget() {
		return target;
	}
}
