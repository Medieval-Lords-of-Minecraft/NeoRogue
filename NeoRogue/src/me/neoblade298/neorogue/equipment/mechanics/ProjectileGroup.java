package me.neoblade298.neorogue.equipment.mechanics;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class ProjectileGroup {
	private ArrayList<Projectile> group = new ArrayList<Projectile>();
	
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
	
	public ArrayList<ProjectileInstance> startWithoutEvent(FightData owner) {
		ArrayList<ProjectileInstance> projs = new ArrayList<ProjectileInstance>(group.size());
		for (Projectile proj : group) {
			projs.add(proj.start(owner));
		}
		return projs;
	}
	
	public ArrayList<ProjectileInstance> start(FightData owner) {
		ArrayList<ProjectileInstance> projs = new ArrayList<ProjectileInstance>(group.size());
		if (owner.getEntity() instanceof Player) {
			Player p = (Player) owner.getEntity();
			FightInstance.trigger(p, Trigger.LAUNCH_PROJECTILE_GROUP, new LaunchProjectileGroupEvent(this));
		}
		for (Projectile proj : group) {
			projs.add(proj.start(owner));
		}
		return projs;
	}
}
