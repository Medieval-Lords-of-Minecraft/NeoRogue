package me.neoblade298.neorogue.equipment.mechanics;

import java.util.LinkedList;

import org.bukkit.entity.Player;

import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class ProjectileGroup {
	private LinkedList<Projectile> group = new LinkedList<Projectile>();
	
	public ProjectileGroup(Projectile... projs) {
		for (Projectile proj : projs) {
			group.add(proj);
		}
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
	
	public LinkedList<ProjectileInstance> start(FightData owner) {
		LinkedList<ProjectileInstance> projs = new LinkedList<ProjectileInstance>();
		for (Projectile proj : group) {
			projs.add(proj.start(owner));
		}
		if (owner.getEntity() instanceof Player) {
			Player p = (Player) owner.getEntity();
			FightInstance.trigger(p, Trigger.LAUNCH_PROJECTILE_GROUP, new LaunchProjectileGroupEvent(this, projs));
		}
		return projs;
	}
}
