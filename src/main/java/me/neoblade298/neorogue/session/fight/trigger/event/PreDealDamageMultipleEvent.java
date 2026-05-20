package me.neoblade298.neorogue.session.fight.trigger.event;

import java.util.Collection;

import org.bukkit.entity.LivingEntity;

import me.neoblade298.neorogue.session.fight.DamageMeta;

public class PreDealDamageMultipleEvent {
	private DamageMeta meta;
	private Collection<LivingEntity> targets;
	public DamageMeta getMeta() {
		return meta;
	}
	
	public PreDealDamageMultipleEvent(DamageMeta meta, Collection<LivingEntity> targets) {
		super();
		this.meta = meta;
		this.targets = targets;
	}
	
	public Collection<LivingEntity> getTargets() {
		return this.targets;
	}
}
