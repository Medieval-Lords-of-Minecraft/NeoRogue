package me.neoblade298.neorogue.session.fight.trigger.event;

import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.mechanics.IProjectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;

public class PreLaunchProjectileGroupEvent {
	private ProjectileGroup group;
	public PreLaunchProjectileGroupEvent(ProjectileGroup group) {
		this.group = group;
	}
	public ProjectileGroup getGroup() {
		return group;
	}
	public boolean isBowProjectile() {
		return group.getFirst() instanceof BowProjectile;
	}

	public boolean isBasicAttack() {
		IProjectile proj = group.getFirst();
		return proj instanceof BowProjectile && ((BowProjectile) proj).isBasicAttack();
	}
}
