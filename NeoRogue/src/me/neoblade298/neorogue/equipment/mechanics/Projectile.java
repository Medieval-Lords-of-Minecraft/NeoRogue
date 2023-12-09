package me.neoblade298.neorogue.equipment.mechanics;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;

public abstract class Projectile {
	protected HashSet<UUID> targetsHit = new HashSet<UUID>();
	protected Vector v;
	protected Location loc;
	protected double gravity;
	protected boolean pierce, ignoreBarriers, ignoreBlocks, ignoreEntities;
	protected BukkitTask task;
	protected int maxTicks;
	protected FightInstance inst;
	protected FightData owner;
	protected BoundingBox bounds;
	public Projectile(LivingEntity origin, double blocksPerTick, double maxRange, int tickSpeed, boolean pierce, boolean ignoreBarriers,
			boolean ignoreBlocks, boolean ignoreEntities, double yRotate, double gravity, FightInstance inst, FightData owner, double x, double y, double z) {
		this.gravity = gravity;
		this.pierce = pierce;
		this.ignoreBarriers = ignoreBarriers;
		this.maxTicks = (int) (maxRange / blocksPerTick) + 1;
		this.inst = inst;
		this.owner = owner;
		
		v = origin.getEyeLocation().getDirection().rotateAroundY(Math.toRadians(yRotate));
		v.multiply(blocksPerTick);
		
		loc = origin.getLocation().add(0, 1.5, 0);
		bounds = BoundingBox.of(loc, x, y, z);
		
		task = new BukkitRunnable() {
			private int count = 0;
			public void run() {
				if (tick() || ++count > maxTicks) {
					onEnd();
					cancel();
				}
			}
		}.runTaskTimer(NeoRogue.inst(), tickSpeed, tickSpeed);
	}
	
	// True to cancel runnable
	private boolean tick() {
		loc.add(v);
		bounds.shift(v);
		
		// Check for collision with shields
		if (!ignoreBarriers) {
			for (Barrier b : inst.getEnemyBarriers().values()) {
				if (b.collides(loc)) {
					onHit(FightInstance.getFightData(b.getOwner().getUniqueId()), b);
					Player p = owner.getEntity() instanceof Player ? (Player) owner.getEntity() : null;
					Util.playSound(p, Sound.ITEM_SHIELD_BLOCK, 1F, 1F, true);
					return true;
				}
			}
		}
		
		// Check for collision with mobs
		if (!ignoreEntities) {
			for (Entity ent : loc.getWorld().getNearbyEntities(bounds)) {
				if (ent instanceof Player) continue;
				
				UUID uuid = ent.getUniqueId();
				FightData hit = FightInstance.getFightData(uuid);
				if (targetsHit.contains(uuid)) continue;
				targetsHit.add(uuid);
				
				if (!pierce) {
					onHit(hit, null);
					return true;
				}
				else {
					onHit(hit, null);
				}
			}
		}
		
		// Check for collision with blocks
		if (!ignoreBlocks) {
			Block b = loc.getBlock();
			if (!b.isPassable()) {
				for (BoundingBox block : loc.getBlock().getCollisionShape().getBoundingBoxes()) {
					block.shift(b.getLocation());
					if (bounds.overlaps(block)) {
						return true;
					}
				}
			}
		}
		
		// Tick after making sure there's no collisions
		onTick();
		
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
	
	public abstract void onTick();
	public abstract void onEnd();
	public abstract void onHit(FightData hit, Barrier hitBarrier);
}
