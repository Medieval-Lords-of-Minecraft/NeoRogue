package me.neoblade298.neorogue.session.fight.trigger.event;

import java.util.LinkedList;

import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.mechanics.IProjectileInstance;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;

public class LaunchProjectileGroupEvent {
	private ProjectileGroup group;
	private LinkedList<IProjectileInstance> insts;
	public LaunchProjectileGroupEvent(ProjectileGroup group, LinkedList<IProjectileInstance> insts) {
		this.group = group;
		this.insts = insts;
	}
	public ProjectileGroup getGroup() {
		return group;
	}
	public void setGroup(ProjectileGroup group) {
		this.group = group;
	}
	public LinkedList<IProjectileInstance> getInstances() {
		return insts;
	}
	// Should only be true if used as a basic attack
	public boolean isBowProjectile() {
		return insts.getFirst().getParent() instanceof BowProjectile;
	}
}
