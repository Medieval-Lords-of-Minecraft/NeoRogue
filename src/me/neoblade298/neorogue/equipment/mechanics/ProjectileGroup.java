package me.neoblade298.neorogue.equipment.mechanics;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class ProjectileGroup {
	private LinkedList<IProjectile> group = new LinkedList<IProjectile>();
	
	public ProjectileGroup(IProjectile... projs) {
		for (IProjectile proj : projs) {
			group.add(proj);
		}
	}
	
	public void add(IProjectile... projs) {
		for (IProjectile proj : projs) {
			group.add(proj);
		}
	}
	
	public LinkedList<IProjectileInstance> startWithoutEvent(FightData owner) {
		LinkedList<IProjectileInstance> projs = new LinkedList<IProjectileInstance>();
		for (IProjectile proj : group) {
			projs.add(proj.start(owner));
		}
		return projs;
	}
	
	public LinkedList<IProjectileInstance> startWithoutEvent(FightData owner, Location origin, Vector direction) {
		LinkedList<IProjectileInstance> projs = new LinkedList<IProjectileInstance>();
		for (IProjectile proj : group) {
			projs.add(proj.start(owner, origin, direction));
		}
		return projs;
	}
	
	public LinkedList<IProjectileInstance> start(FightData owner) {
		LinkedList<IProjectileInstance> projs = new LinkedList<IProjectileInstance>();
		for (IProjectile proj : group) {
			projs.add(proj.startWithoutEvent(owner));
		}
		if (owner.getEntity() instanceof Player) {
			Player p = (Player) owner.getEntity();
			FightInstance.trigger(p, Trigger.LAUNCH_PROJECTILE_GROUP, new LaunchProjectileGroupEvent(this, projs));
		}
		return projs;
	}
	
	public LinkedList<IProjectileInstance> start(FightData owner, Location origin, Vector direction) {
		direction.normalize();
		LinkedList<IProjectileInstance> projs = new LinkedList<IProjectileInstance>();
		for (IProjectile proj : group) {
			projs.add(proj.startWithoutEvent(owner, origin, direction));
		}
		if (owner.getEntity() instanceof Player) {
			Player p = (Player) owner.getEntity();
			FightInstance.trigger(p, Trigger.LAUNCH_PROJECTILE_GROUP, new LaunchProjectileGroupEvent(this, projs));
		}
		return projs;
	}
}
