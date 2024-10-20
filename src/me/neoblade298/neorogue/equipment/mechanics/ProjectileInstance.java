package me.neoblade298.neorogue.equipment.mechanics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;

public class ProjectileInstance extends IProjectileInstance {
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
	private String tag; // Used for metadata, like with twinShiv
	private DamageMeta meta;
	private double distance = 0, distancePerPoint; // Estimated distance traveled, not exact, doesn't factor in gravity
	private int maxRangeMod;

	private ArrayList<HitBlockAction> hitBlockActions = new ArrayList<HitBlockAction>();
	private ArrayList<HitAction> hitActions = new ArrayList<HitAction>();
	
	protected ProjectileInstance(Projectile settings, FightData owner) {
		this(settings, owner, owner.getEntity().getLocation().add(0, 1.5, 0), owner.getEntity().getLocation().getDirection());
	}
	
	protected ProjectileInstance(Projectile settings, FightData owner, Location origin, Vector direction) {
		super(origin);
		this.inst = owner.getInstance();
		this.owner = owner;
		this.settings = settings;
		this.meta = new DamageMeta(owner, DamageOrigin.PROJECTILE);
		
		v = direction.clone().normalize().rotateAroundY(Math.toRadians(settings.getRotation()));
		if (settings.initialY() != 0) origin.add(0, settings.initialY(), 0);

		// Slow projectile, no interpolation needed
		if (settings.getWidth() > settings.getBlocksPerTick() * settings.getTickSpeed()) {
			v = v.multiply(settings.getBlocksPerTick() * settings.getTickSpeed());
		}
		// Fast projectile, need interpolation
		else {
			v = v.multiply(settings.getWidth());
			interpolationPoints = (int) (settings.getBlocksPerTick() / settings.getWidth()) + 1;
		}
		distancePerPoint = v.length();
		loc = origin.clone().add(v.clone().multiply(0.5)); // Start slightly offset forward to avoid hitting behind
		bounds = BoundingBox.of(loc, settings.getWidth(), settings.getHeight(), settings.getWidth());
		
		task = new BukkitRunnable() {
			public void run() {
				if (tick()) {
					cancel();
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 0L, settings.getTickSpeed());
		owner.addTask(task);
	}

	public DamageMeta getMeta() {
		return meta;
	}

	public void addHitBlockAction(HitBlockAction action) {
		hitBlockActions.add(action);
	}

	public void addHitAction(HitAction action) {
		hitActions.add(action);
	}

	@Override
	public Projectile getParent() {
		return this.settings;
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
						Sounds.block.play(p, loc);
						return true;
					}
				}
			}
			
			// Check for collision with mobs
			if (!settings.isIgnoreEntities()) {
				for (Entity ent : loc.getWorld().getNearbyEntities(bounds)) {
					if (ent instanceof Player || ent.getType() == EntityType.ARMOR_STAND) continue;
					if (!(ent instanceof LivingEntity)) continue;
					
					UUID uuid = ent.getUniqueId();
					FightData hit = FightInstance.getFightData(uuid);
					if (hit == null) continue;
					if (targetsHit.contains(uuid)) continue;
					targetsHit.add(uuid);

					// Make sure to never use the same damage meta twice
					DamageMeta clone = meta.clone();
					settings.onHit(hit, null, this);
					for (HitAction act : hitActions) {
						act.onHit(hit, null, this);
					}
					numHit++;
					meta = clone;
					
					int limit = settings.getPierceLimit();
					if (limit != -1 && numHit >= settings.getPierceLimit()) return true;
				}
			}
			
			// Check for collision with blocks
			if (!settings.isIgnoreBlocks()) {
				Block b = loc.getBlock();
				if (!b.isPassable()) {
					for (BoundingBox hitbox : loc.getBlock().getCollisionShape().getBoundingBoxes()) {
						hitbox.shift(b.getLocation());
						if (bounds.overlaps(hitbox)) {
							settings.onHitBlock(this, b);
							
							for (HitBlockAction act : hitBlockActions) {
								act.onHitBlock(this, b);
							}
							return true;
						}
					}
				}
			}

			settings.onTick(this, i);
			loc.add(v);
			bounds.shift(v);
			distance += distancePerPoint;
			if (distance >= settings.getMaxRange() + maxRangeMod) {
				settings.onFizzle(this);
				return true;
			}
		}
		
		// Gravity
		if (settings.getGravity() != 0) {
			v.setY(v.getY() - settings.getGravity());
		}

		return false;
	}
	
	public void cancel() {
		task.cancel();
	}
	
	public Vector getVelocity() {
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
	
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public String getTag() {
		return this.tag;
	}
	
	public interface HitBlockAction {
		public void onHitBlock(ProjectileInstance proj, Block b);
	}
	
	public interface HitAction {
		public void onHit(FightData hit, Barrier hitBarrier, ProjectileInstance proj);
	}

	public void addMaxRange(int range) {
		this.maxRangeMod += range;
	}
}
