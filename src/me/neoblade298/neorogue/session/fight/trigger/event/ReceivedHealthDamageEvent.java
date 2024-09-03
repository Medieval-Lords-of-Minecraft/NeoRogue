package me.neoblade298.neorogue.session.fight.trigger.event;

import org.bukkit.entity.LivingEntity;

import me.neoblade298.neorogue.session.fight.DamageMeta;

public class ReceivedHealthDamageEvent {
	private LivingEntity damager;
	private DamageMeta meta;
	private double damage, ignoreShieldsDamage;
	public ReceivedHealthDamageEvent(LivingEntity damager, DamageMeta meta, double damage, double ignoreShieldsDamage) {
		super();
		this.damager = damager;
		this.meta = meta;
		this.damage = damage;
		this.ignoreShieldsDamage = ignoreShieldsDamage;
	}
	// Should not be modified at this point, just for monitoring
	public LivingEntity getDamager() {
		return damager;
	}
	public void setDamager(LivingEntity damager) {
		this.damager = damager;
	}
	public DamageMeta getMeta() {
		return meta;
	}
	public double getTotalDamage() {
		return damage + ignoreShieldsDamage;
	}
	public double getDamage() {
		return damage;
	}
	public double getIgnoreShieldsDamage() {
		return ignoreShieldsDamage;
	}
}
