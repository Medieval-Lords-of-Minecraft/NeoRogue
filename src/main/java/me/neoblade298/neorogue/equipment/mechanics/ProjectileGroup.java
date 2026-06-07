package me.neoblade298.neorogue.equipment.mechanics;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreLaunchProjectileGroupEvent;

public class ProjectileGroup {
	private LinkedList<Projectile> group = new LinkedList<Projectile>();
	
	public ProjectileGroup(Projectile... projs) {
		for (Projectile proj : projs) {
			group.add(proj);
		}
	}

	public LinkedList<Projectile> list() {
		return group;
	}

	public Projectile getFirst() {
		return group.getFirst();
	}
	
	public void add(Projectile... projs) {
		for (Projectile proj : projs) {
			group.add(proj);
		}
	}
	
	public LinkedList<ProjectileInstance> startWithoutEvent(FightData owner) {
		LinkedList<ProjectileInstance> projs = new LinkedList<ProjectileInstance>();
		for (Projectile proj : group) {
			projs.add(proj.start(owner));
		}
		return projs;
	}
	
	public LinkedList<ProjectileInstance> startWithoutEvent(FightData owner, Location origin, Vector direction) {
		LinkedList<ProjectileInstance> projs = new LinkedList<ProjectileInstance>();
		for (Projectile proj : group) {
			projs.add(proj.start(owner, origin, direction));
		}
		return projs;
	}
	
	public LinkedList<ProjectileInstance> start(FightData owner) {
		Player p = (Player) owner.getEntity();
		return start(owner, p.getLocation().add(0, p.isSneaking() ? 1.0 : 1.4, 0), p.getEyeLocation().getDirection());
	}
	
	public LinkedList<ProjectileInstance> start(FightData owner, Location origin, Vector direction) {
		PreLaunchProjectileGroupEvent event = null;
		if (owner.getEntity() instanceof Player) {
			Player p = (Player) owner.getEntity();
			event = new PreLaunchProjectileGroupEvent(this);
			if (FightInstance.trigger(p, Trigger.PRE_LAUNCH_PROJECTILE_GROUP, event)) return null;
		}

		direction.normalize();
		LinkedList<ProjectileInstance> projs = new LinkedList<ProjectileInstance>();
		for (Projectile proj : group) {
			projs.add(proj.start(owner, origin, direction, event));
		}

		if (owner.getEntity() instanceof Player) {
			Player p = (Player) owner.getEntity();
			FightInstance.trigger(p, Trigger.LAUNCH_PROJECTILE_GROUP, new LaunchProjectileGroupEvent(this, projs));
		}
		return projs;
	}
}
