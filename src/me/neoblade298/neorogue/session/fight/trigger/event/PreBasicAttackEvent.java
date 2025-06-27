package me.neoblade298.neorogue.session.fight.trigger.event;

import org.bukkit.entity.LivingEntity;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageMeta;

public class PreBasicAttackEvent {
	private LivingEntity target;
	private DamageMeta meta;
	private Equipment weapon;
	private ProjectileInstance proj;
	public PreBasicAttackEvent(LivingEntity target, DamageMeta meta, Equipment weapon, ProjectileInstance proj) {
		this.target = target;
		this.meta = meta;
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
