package me.neoblade298.neorogue.session.fight.trigger.event;

import java.util.Collection;

import org.bukkit.entity.LivingEntity;

import me.neoblade298.neorogue.session.fight.DamageMeta;

public class DealtDamageEvent {
	private DamageMeta meta;
	private LivingEntity target;
	private double totalDamage, damage, ignoreShieldsDamage;
	public double getTotalDamage() {
		return totalDamage;
	}
	public double getDamage() {
		return damage;
	}
	public double getIgnoreShieldsDamage() {
		return ignoreShieldsDamage;
	}
	public DamageMeta getMeta() {
		return meta;
	}
	public DealtDamageEvent(DamageMeta meta, LivingEntity target, double damage, double ignoreShieldsDamage) {
		super();
		this.meta = meta;
		this.target = target;
		this.damage = damage;
		this.ignoreShieldsDamage = ignoreShieldsDamage;
		this.totalDamage = damage + ignoreShieldsDamage;
	}
	
	public LivingEntity getTarget() {
		return this.target;
	}
}
