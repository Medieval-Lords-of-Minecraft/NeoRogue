package me.neoblade298.neorogue.equipment.mechanics;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.session.fight.FightData;

public abstract class IProjectile {
	protected IProjectileInstance start(FightData owner, Location source, Vector direction) {
		return create(owner, source.clone(), direction);
	}
	protected IProjectileInstance start(FightData owner) {
		return create(owner, owner.getEntity().getLocation().add(0, 1.65, 0), owner.getEntity().getEyeLocation().getDirection());
	}
	protected abstract IProjectileInstance create(FightData owner, Location source, Vector direction);
}
