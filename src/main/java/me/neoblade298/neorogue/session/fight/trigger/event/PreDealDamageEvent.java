package me.neoblade298.neorogue.session.fight.trigger.event;

import org.bukkit.entity.LivingEntity;

import me.neoblade298.neorogue.session.fight.DamageMeta;

public class PreDealDamageEvent {
	private DamageMeta meta;
	private LivingEntity target;
	public DamageMeta getMeta() {
		return meta;
	}
	public PreDealDamageEvent(DamageMeta meta, LivingEntity target) {
		super();
		this.meta = meta;
		this.target = target;
	}
	
	public LivingEntity getTarget() {
		return this.target;
	}
}
