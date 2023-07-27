package me.neoblade298.neorogue.equipment.mechanics;

import java.util.ArrayList;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class Projectile {
	private ArrayList<Entity> targetsHit = new ArrayList<Entity>();
	private ProjectileCallback callback;
	private Vector v;
	public Projectile(LivingEntity origin, double blocksPerTick, int maxRange, int tickSpeed, boolean pierce, boolean ignoreBarriers,
			double yRotate, double gravity, ProjectileCallback callback) {
		this.callback = callback;
		v = origin.getEyeLocation().getDirection().rotateAroundY(Math.toRadians(yRotate));
		v.multiply(blocksPerTick);
	}
	
	private void tick() {
		
	}
}
