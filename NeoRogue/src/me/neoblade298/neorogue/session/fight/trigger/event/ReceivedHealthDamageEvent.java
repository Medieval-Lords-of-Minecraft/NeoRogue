package me.neoblade298.neorogue.session.fight.trigger.event;

import org.bukkit.entity.LivingEntity;

import me.neoblade298.neorogue.session.fight.DamageMeta;

public class ReceivedHealthDamageEvent {
	private LivingEntity damager;
	private DamageMeta meta;
	public LivingEntity getDamager() {
		return damager;
	}
	public void setDamager(LivingEntity damager) {
		this.damager = damager;
	}
	public DamageMeta getMeta() {
		return meta;
	}
	public void setMeta(DamageMeta meta) {
		this.meta = meta;
	}
	public ReceivedHealthDamageEvent(LivingEntity damager, DamageMeta meta) {
		super();
		this.damager = damager;
		this.meta = meta;
	}
}
