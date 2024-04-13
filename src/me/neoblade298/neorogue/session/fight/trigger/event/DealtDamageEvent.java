package me.neoblade298.neorogue.session.fight.trigger.event;

import java.util.Collection;

import org.bukkit.entity.LivingEntity;

import me.neoblade298.neorogue.session.fight.DamageMeta;

public class DealtDamageEvent {
	private DamageMeta meta;
	private LivingEntity target;
	private Collection<LivingEntity> targets;
	public DamageMeta getMeta() {
		return meta;
	}
	// DEALT_DAMAGE_MULTIPLE
	public DealtDamageEvent(DamageMeta meta, Collection<LivingEntity> targets) {
		super();
		this.meta = meta;
		this.targets = targets;
	}
	// DEALT_DAMAGE
	public DealtDamageEvent(DamageMeta meta, LivingEntity target) {
		super();
		this.meta = meta;
		this.target = target;
	}
	
	public LivingEntity getTarget() {
		return this.target;
	}
	
	// Only used for DEALT_DAMAGE_MULTIPLE event
	public Collection<LivingEntity> getTargets() {
		return this.targets;
	}
}
