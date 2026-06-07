package me.neoblade298.neorogue.session.fight.trigger.event;

import java.util.LinkedList;

import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.session.fight.buff.BuffList;

public class PreLaunchProjectileGroupEvent {
	private ProjectileGroup group;
	private LinkedList<Projectile> projectiles;
	private BuffList velocityBuff = new BuffList();
	public PreLaunchProjectileGroupEvent(ProjectileGroup group) {
		this.group = group;
		this.projectiles = group.list();
	}
	public ProjectileGroup getGroup() {
		return group;
	}
	public LinkedList<Projectile> getProjectiles() {
		return projectiles;
	}
	public BuffList getVelocityBuffList() {
		return velocityBuff;
	}
	public boolean isBowProjectile() {
		return group.getFirst() instanceof BowProjectile;
	}

	public boolean isBasicAttack() {
		Projectile proj = group.getFirst();
		return proj instanceof BowProjectile && ((BowProjectile) proj).isBasicAttack();
	}
}
