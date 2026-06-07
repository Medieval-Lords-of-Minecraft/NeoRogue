package me.neoblade298.neorogue.session.fight.trigger.event;

import java.util.LinkedList;

import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;

public class LaunchProjectileGroupEvent {
	private ProjectileGroup group;
	private LinkedList<ProjectileInstance> insts;
	public LaunchProjectileGroupEvent(ProjectileGroup group, LinkedList<ProjectileInstance> insts) {
		this.group = group;
		this.insts = insts;
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
		Projectile proj = group.getFirst();
		return proj instanceof BowProjectile && ((BowProjectile) proj).isBasicAttack();
	}
}
