package me.neoblade298.neorogue.equipment.mechanics;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public abstract class ProjectileCallback {
	public abstract void onHit(Entity caster, Entity hit);
	public abstract void onEnd(Entity caster, Location hit);
}
