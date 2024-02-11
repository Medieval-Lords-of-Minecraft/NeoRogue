package me.neoblade298.neorogue.session.fight.trigger.event;

import org.bukkit.entity.LivingEntity;

public class LeftClickHitEvent {
	private LivingEntity target;

	public LivingEntity getTarget() {
		return target;
	}

	public void setTarget(LivingEntity target) {
		this.target = target;
	}

	public LeftClickHitEvent(LivingEntity target) {
		super();
		this.target = target;
	}
}
