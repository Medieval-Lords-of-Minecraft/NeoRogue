package me.neoblade298.neorogue.session.fight.trigger.event;

import org.bukkit.entity.LivingEntity;

public class RightClickHitEvent {
	private LivingEntity target;

	public LivingEntity getTarget() {
		return target;
	}

	public void setTarget(LivingEntity target) {
		this.target = target;
	}

	public RightClickHitEvent(LivingEntity target) {
		super();
		this.target = target;
	}
}
