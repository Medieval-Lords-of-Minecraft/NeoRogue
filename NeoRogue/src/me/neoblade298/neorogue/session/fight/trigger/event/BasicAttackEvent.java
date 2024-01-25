package me.neoblade298.neorogue.session.fight.trigger.event;

import org.bukkit.entity.LivingEntity;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageMeta;

public class BasicAttackEvent {
	private LivingEntity target;
	private double knockback;
	private DamageMeta meta;
	private Equipment weapon;
	private ProjectileInstance proj;
	public BasicAttackEvent(LivingEntity target, DamageMeta meta, double knockback, Equipment weapon, ProjectileInstance proj) {
		this.target = target;
		this.meta = meta;
		this.knockback = knockback;
		this.weapon = weapon;
		this.proj = proj;
	}
	public LivingEntity getTarget() {
		return target;
	}
	public void setTarget(LivingEntity target) {
		this.target = target;
	}
	public DamageMeta getMeta() {
		return meta;
	}
	public double getKnockback() {
		return knockback;
	}
	public void setKnockback(double knockback) {
		this.knockback = knockback;
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
