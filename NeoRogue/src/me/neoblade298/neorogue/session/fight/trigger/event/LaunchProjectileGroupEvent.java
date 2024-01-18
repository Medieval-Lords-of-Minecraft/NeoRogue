package me.neoblade298.neorogue.session.fight.trigger.event;

import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;

public class LaunchProjectileGroupEvent {
	private ProjectileGroup group;
	public LaunchProjectileGroupEvent(ProjectileGroup group) {
		this.group = group;
	}
	public ProjectileGroup getGroup() {
		return group;
	}
	public void setGroup(ProjectileGroup group) {
		this.group = group;
	}
}
