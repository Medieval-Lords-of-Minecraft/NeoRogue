package me.neoblade298.neorogue.equipment.mechanics;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.buff.BuffList;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreLaunchProjectileGroupEvent;

public class ProjectileGroup {
	private LinkedList<IProjectile> group = new LinkedList<IProjectile>();
	
	public ProjectileGroup(IProjectile... projs) {
		for (IProjectile proj : projs) {
			group.add(proj);
		}
	}

	public LinkedList<IProjectile> list() {
		return group;
	}

	public IProjectile getFirst() {
		return group.getFirst();
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
		Player p = (Player) owner.getEntity();
		return start(owner, p.getLocation().add(0, p.isSneaking() ? 1.0 : 1.4, 0), p.getEyeLocation().getDirection());
	}
	
	public LinkedList<IProjectileInstance> start(FightData owner, Location origin, Vector direction) {
		BuffList velocityBuff = null;
		if (owner.getEntity() instanceof Player) {
			Player p = (Player) owner.getEntity();
			PreLaunchProjectileGroupEvent event = new PreLaunchProjectileGroupEvent(this);
			if (FightInstance.trigger(p, Trigger.PRE_LAUNCH_PROJECTILE_GROUP, event)) return null;
			velocityBuff = event.getVelocityBuffList();
		}

		direction.normalize();
		LinkedList<IProjectileInstance> projs = new LinkedList<IProjectileInstance>();
		for (IProjectile proj : group) {
			IProjectileInstance inst = proj.startWithoutEvent(owner, origin, direction);
			if (velocityBuff != null && inst instanceof ProjectileInstance pi) {
				pi.setBlocksPerTick(velocityBuff.apply(pi.getBlocksPerTick()));
			}
			projs.add(inst);
		}

		if (owner.getEntity() instanceof Player) {
			Player p = (Player) owner.getEntity();
			FightInstance.trigger(p, Trigger.LAUNCH_PROJECTILE_GROUP, new LaunchProjectileGroupEvent(this, projs));
		}
		return projs;
	}
}
