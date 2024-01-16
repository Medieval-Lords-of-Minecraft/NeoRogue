package me.neoblade298.neorogue.equipment.mechanics;

import java.util.HashMap;
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
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;

public class Projectile {
	private FightInstance inst;
	private FightData owner;
	private HashSet<UUID> targetsHit = new HashSet<UUID>();
	private Vector v;
	private ProjectileSettings settings;
	private BukkitTask task;
	private Location loc;
	private BoundingBox bounds;
	private HashMap<BuffType, Buff> buffs;
	
	public Projectile(ProjectileSettings settings, FightInstance inst, FightData owner) {
		this.inst = inst;
		this.owner = owner;
		LivingEntity origin = owner.getEntity();
		
		v = origin.getEyeLocation().getDirection().rotateAroundY(Math.toRadians(settings.getRotation()));
		v.multiply(settings.getBlocksPerTick());
		loc = origin.getEyeLocation();
		bounds = settings.getBounds().clone().shift(loc);
		
		task = new BukkitRunnable() {
			private int count = 0;
			public void run() {
				if (tick() || ++count > settings.getMaxTicks()) {
					settings.onEnd(loc);
					cancel();
				}
			}
		}.runTaskTimer(NeoRogue.inst(), settings.getTickSpeed(), settings.getTickSpeed());
	}
	
	// True to cancel runnable
	private boolean tick() {
		loc.add(v);
		bounds.shift(v);
		
		// Check for collision with shields
		if (!settings.isIgnoreBarriers()) {
			for (Barrier b : inst.getEnemyBarriers().values()) {
				if (b.collides(loc)) {
					settings.onHit(FightInstance.getFightData(b.getOwner().getUniqueId()), b, this);
					Player p = owner.getEntity() instanceof Player ? (Player) owner.getEntity() : null;
					Util.playSound(p, Sound.ITEM_SHIELD_BLOCK, 1F, 1F, true);
					return true;
				}
			}
		}
		
		// Check for collision with mobs
		if (!settings.isIgnoreEntities()) {
			for (Entity ent : loc.getWorld().getNearbyEntities(bounds)) {
				if (ent instanceof Player) continue;
				
				UUID uuid = ent.getUniqueId();
				FightData hit = FightInstance.getFightData(uuid);
				if (targetsHit.contains(uuid)) continue;
				targetsHit.add(uuid);
				
				if (!settings.isPiercing()) {
					settings.onHit(hit, null, this);
					return true;
				}
				else {
					settings.onHit(hit, null, this);
				}
			}
		}
		
		// Check for collision with blocks
		if (!settings.isIgnoreBlocks()) {
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
		settings.onTick(loc);
		
		// Gravity
		if (settings.getGravity() != 0) {
			v.setY(v.getY() - settings.getGravity());
		}
		return false;
	}
	
	public void cancel(boolean useOnEnd) {
		task.cancel();
		if (useOnEnd) settings.onEnd(loc);
	}
	
	public Vector getVector() {
		return v;
	}
	
	public HashMap<BuffType, Buff> getBuffs() {
		return buffs;
	}
	
	public FightData getOwner() {
		return owner;
	}
}
