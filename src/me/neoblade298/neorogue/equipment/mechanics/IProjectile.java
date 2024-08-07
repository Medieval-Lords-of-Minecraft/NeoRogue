package me.neoblade298.neorogue.equipment.mechanics;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.session.fight.FightData;

public abstract class IProjectile {
	protected abstract IProjectileInstance start(FightData owner, Location source, Vector direction);
	protected IProjectileInstance start(FightData owner) {
		return start(owner, owner.getEntity().getLocation().add(0, 1, 0), owner.getEntity().getEyeLocation().getDirection());
	}
}
