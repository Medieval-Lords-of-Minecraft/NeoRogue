package me.neoblade298.neorogue.session.fight.trigger.event;

import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;

public class ShootBowEvent {
	private BowProjectile proj;
	private ProjectileInstance inst;

	public ShootBowEvent(BowProjectile proj, ProjectileInstance inst) {
		this.proj = proj;
		this.inst = inst;
	}

	public BowProjectile getProjectile() {
		return proj;
	}

	public ProjectileInstance getInstance() {
		return inst;
	}
}
