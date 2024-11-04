package me.neoblade298.neorogue.equipment.mechanics;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.session.fight.FightData;

public abstract class IProjectile {
	protected IProjectileInstance startWithoutEvent(FightData owner, Location source, Vector direction) {
		return create(owner, source.clone(), direction);
	}
	protected IProjectileInstance startWithoutEvent(FightData owner) {
		return startWithoutEvent(owner, owner.getEntity().getLocation().add(0, 1.5, 0), owner.getEntity().getEyeLocation().getDirection());
	}
	public IProjectileInstance start(FightData owner, Location source, Vector direction) {
		LinkedList<IProjectileInstance> insts = new LinkedList<IProjectileInstance>();
		IProjectileInstance inst = create(owner, source.clone(), direction);
		insts.add(inst);
		return inst;
	}
	public IProjectileInstance start(FightData owner) {
		return start(owner, owner.getEntity().getLocation().add(0, 1.5, 0), owner.getEntity().getEyeLocation().getDirection());
	}
	protected abstract IProjectileInstance create(FightData owner, Location source, Vector direction);
}
