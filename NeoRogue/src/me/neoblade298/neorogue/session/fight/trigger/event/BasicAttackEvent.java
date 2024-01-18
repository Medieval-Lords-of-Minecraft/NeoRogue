package me.neoblade298.neorogue.session.fight.trigger.event;

import org.bukkit.entity.LivingEntity;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageType;

public class BasicAttackEvent {
	private LivingEntity target;
	private double damage, knockback;
	private DamageType type;
	private Equipment weapon;
	private ProjectileInstance proj;
	public BasicAttackEvent(LivingEntity target, double damage, double knockback, DamageType type, Equipment weapon, ProjectileInstance proj) {
		this.target = target;
		this.damage = damage;
		this.knockback = knockback;
		this.type = type;
		this.weapon = weapon;
		this.proj = proj;
	}
	public LivingEntity getTarget() {
		return target;
	}
	public void setTarget(LivingEntity target) {
		this.target = target;
	}
	public double getDamage() {
		return damage;
	}
	public void setDamage(double damage) {
		this.damage = damage;
	}
	public double getKnockback() {
		return knockback;
	}
	public void setKnockback(double knockback) {
		this.knockback = knockback;
	}
	public DamageType getType() {
		return type;
	}
	public void setType(DamageType type) {
		this.type = type;
	}
	public Equipment getWeapon() {
		return weapon;
	}
	public void setWeapon(Equipment weapon) {
		this.weapon = weapon;
	}
	public ProjectileInstance getProjectile() {
		return proj;
	}
	public boolean isProjectile() {
		return proj != null;
	}
}
