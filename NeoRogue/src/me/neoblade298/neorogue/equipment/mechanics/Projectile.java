package me.neoblade298.neorogue.equipment.mechanics;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.fights.FightInstance;

public abstract class Projectile {
	private ArrayList<Entity> targetsHit = new ArrayList<Entity>();
	private Vector v;
	private Location loc;
	private double gravity;
	private boolean pierce, ignoreBarriers;
	private BukkitTask task;
	private int maxTicks;
	private FightInstance inst;
	public Projectile(LivingEntity origin, double blocksPerTick, double maxRange, int tickSpeed, boolean pierce, boolean ignoreBarriers,
			double yRotate, double gravity, FightInstance inst) {
		this.gravity = gravity;
		this.pierce = pierce;
		this.ignoreBarriers = ignoreBarriers;
		this.maxTicks = (int) (maxRange / blocksPerTick) + 1;
		this.inst = inst;
		
		v = origin.getEyeLocation().getDirection().rotateAroundY(Math.toRadians(yRotate));
		v.multiply(blocksPerTick);
		
		loc = origin.getLocation().add(0, 1, 0);
		
		task = new BukkitRunnable() {
			private int count = 0;
			public void run() {
				if (tick() || ++count > maxTicks) cancel();
			}
		}.runTaskTimer(NeoRogue.inst(), tickSpeed, tickSpeed);
	}
	
	// True to cancel runnable
	private boolean tick() {
		loc.add(v);
		
		// Check for collision with shields
		if (!ignoreBarriers) {
			for (Barrier b : inst.getBarriers().values()) {
				if (b.collides(loc)) {
					onHit(FightInstance.getFightData(b.getOwner().getUniqueId())
				}
			}
		}
		
		// Gravity
		if (gravity != 0) {
			v.setY(v.getY() - gravity);
		}
		return false;
	}
	
	public void cancel(boolean useOnEnd) {
		task.cancel();
		if (useOnEnd) onEnd();
	}
	
	public abstract void onEnd();
	public abstract void onHit(FightData hit, FightData owner, Barrier hitBarrier);
}
