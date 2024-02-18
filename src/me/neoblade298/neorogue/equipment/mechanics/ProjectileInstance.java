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

public class ProjectileInstance {
	private FightInstance inst;
	private FightData owner;
	private HashSet<UUID> targetsHit = new HashSet<UUID>();
	private Vector v;
	private Projectile settings;
	private BukkitTask task;
	private Location loc;
	private BoundingBox bounds;
	private HashMap<BuffType, Buff> buffs = new HashMap<BuffType, Buff>();
	private int tick, numHit, interpolationPoints;
	
	protected ProjectileInstance(Projectile settings, FightData owner) {
		this.inst = owner.getInstance();
		this.owner = owner;
		this.settings = settings;
		LivingEntity origin = owner.getEntity();
		
		v = origin.getLocation().getDirection().rotateAroundY(Math.toRadians(settings.getRotation()));
		if (settings.initialY() != 0) v = v.add(new Vector(0, settings.initialY(), 0)).normalize();
		v.multiply(settings.getBlocksPerTick() * settings.getTickSpeed());
		interpolationPoints = (int) v.length() + 1;
		loc = origin.getLocation().add(0, 1, 0);
		bounds = BoundingBox.of(loc, settings.getWidth(), settings.getHeight(), settings.getWidth());
		
		task = new BukkitRunnable() {
			public void run() {
				if (tick()) {
					cancel();
				}
			}
		}.runTaskTimer(NeoRogue.inst(), settings.getTickSpeed(), settings.getTickSpeed());
	}
	
	protected ProjectileInstance(Projectile settings, FightData owner, Location origin, Vector direction) {
		this.inst = owner.getInstance();
		this.owner = owner;
		this.settings = settings;
		
		v = direction.rotateAroundY(Math.toRadians(settings.getRotation()));
		if (settings.initialY() != 0) v = v.add(new Vector(0, settings.initialY(), 0)).normalize();
		v.multiply(settings.getBlocksPerTick() * settings.getTickSpeed());
		interpolationPoints = (int) v.length() + 1;
		loc = origin.add(0, 1, 0);
		bounds = BoundingBox.of(loc, settings.getWidth(), settings.getHeight(), settings.getWidth());
		
		task = new BukkitRunnable() {
			public void run() {
				if (tick()) {
					cancel();
				}
			}
		}.runTaskTimer(NeoRogue.inst(), settings.getTickSpeed(), settings.getTickSpeed());
	}
	
	public int getTick() {
		return tick;
	}
	
	// True to cancel runnable
	private boolean tick() {
		for (int i = 0; i < interpolationPoints; i++) {
			// Check for collision with shields
			if (!settings.isIgnoreBarriers()) {
				for (Barrier b : inst.getEnemyBarriers().values()) {
					if (b.collides(loc)) {
						numHit++;
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
					if (!(ent instanceof LivingEntity)) continue;
					
					UUID uuid = ent.getUniqueId();
					FightData hit = FightInstance.getFightData(uuid);
					if (targetsHit.contains(uuid)) continue;
					targetsHit.add(uuid);
					
					if (!settings.isPiercing()) {
						numHit++;
						settings.onHit(hit, null, this);
						return true;
					}
					else {
						numHit++;
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
							settings.onHitBlock(this);
							return true;
						}
					}
				}
			}

			settings.onTick(this, i == interpolationPoints - 1);
			loc.add(v);
			bounds.shift(v);
		}
		
		// Gravity
		if (settings.getGravity() != 0) {
			v.setY(v.getY() - settings.getGravity());
		}
		
		if (++tick > settings.getMaxTicks()) {
			settings.onFizzle(this);
			return true;
		}

		return false;
	}
	
	public void cancel() {
		task.cancel();
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
	
	public Location getLocation() {
		return loc;
	}
	
	public int getNumHit() {
		return numHit;
	}
}
