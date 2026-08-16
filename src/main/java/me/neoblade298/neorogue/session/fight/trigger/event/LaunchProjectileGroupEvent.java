package me.neoblade298.neorogue.session.fight.trigger.event;

import java.util.LinkedList;

import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;

public class LaunchProjectileGroupEvent {
	private ProjectileGroup group;
	private LinkedList<ProjectileInstance> insts;
	private boolean aftershot;
	public LaunchProjectileGroupEvent(ProjectileGroup group, LinkedList<ProjectileInstance> insts) {
		this(group, insts, false);
	}
	public LaunchProjectileGroupEvent(ProjectileGroup group, LinkedList<ProjectileInstance> insts, boolean aftershot) {
		this.group = group;
		this.insts = insts;
		this.aftershot = aftershot;
	}
	public ProjectileGroup getGroup() {
		return group;
	}
	public void setGroup(ProjectileGroup group) {
		this.group = group;
	}
	public LinkedList<ProjectileInstance> getInstances() {
		return insts;
	}
	public boolean isBowProjectile() {
		return group.getFirst() instanceof BowProjectile;
	}

	public boolean isBasicAttack() {
		if (aftershot) return false;
		Projectile proj = group.getFirst();
		return proj instanceof BowProjectile && ((BowProjectile) proj).isBasicAttack();
	}
	public boolean isAftershot() {
		return aftershot;
	}
}
