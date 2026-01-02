package me.neoblade298.neorogue.session.fight.trigger.event;

import org.bukkit.entity.LivingEntity;

import me.neoblade298.neorogue.session.fight.DamageMeta;

public class KillEvent {
	private LivingEntity target;
	private DamageMeta dm;
	public KillEvent(LivingEntity target, DamageMeta dm) {
		this.target = target;
		this.dm = dm;
	}
	public LivingEntity getTarget() {
		return target;
	}
	public DamageMeta getDamageMeta() {
		return dm;
	}
}
