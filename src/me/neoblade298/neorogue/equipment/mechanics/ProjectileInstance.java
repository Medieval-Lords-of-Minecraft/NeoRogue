package me.neoblade298.neorogue.equipment.mechanics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import me.libraryaddict.disguise.DisguiseAPI;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.AmmunitionInstance;
import me.neoblade298.neorogue.equipment.Bow;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffList;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;

public class ProjectileInstance extends IProjectileInstance {
	private static final HashMap<EntityType, BoundingBox> entityBounds = new HashMap<EntityType, BoundingBox>();

	private FightInstance inst;
	private FightData owner;
	private HashSet<UUID> targetsHit = new HashSet<UUID>();
	private Vector v;
	private Projectile settings;
	private BukkitTask task;
	private Location loc;
	private BoundingBox bounds, bigBounds;
	private int tick, numHit, interpolationPoints;
	private ActionMeta am = new ActionMeta();
	private String tag; // Used for metadata, like with twinShiv
	private DamageMeta meta;
	private double distance = 0, distancePerPoint; // Estimated distance traveled, not exact, doesn't factor in gravity
	private int maxRangeMod, pierceMod;
	private LivingEntity homingTarget;
	private HashMap<DamageBuffType, BuffList> buffs = new HashMap<DamageBuffType, BuffList>();

	private ArrayList<HitBlockAction> hitBlockActions = new ArrayList<HitBlockAction>();
	private ArrayList<HitAction> hitActions = new ArrayList<HitAction>();

	static {
		// Used for correcting libsdisguises bounding boxes
		// Center must be at exactly 0,0,0 so that shifting them works easily
		BoundingBox zombie = createBox(0.6, 1.95);
		entityBounds.put(EntityType.SPIDER, createBox(1.4, 0.9));
		entityBounds.put(EntityType.WOLF, createBox(0.6, 0.85));
		entityBounds.put(EntityType.SKELETON, createBox(0.6, 2));
		entityBounds.put(EntityType.STRAY, createBox(0.6, 2));
		entityBounds.put(EntityType.WITHER_SKELETON, createBox(0.7, 2.4));
		entityBounds.put(EntityType.ZOMBIE, zombie);
		entityBounds.put(EntityType.HUSK, zombie);
		entityBounds.put(EntityType.DROWNED, zombie);
		entityBounds.put(EntityType.IRON_GOLEM, createBox(1.4, 2.7));
		entityBounds.put(EntityType.BLOCK_DISPLAY, createBox(1, 1));
		entityBounds.put(EntityType.FALLING_BLOCK, createBox(1, 1));
		entityBounds.put(EntityType.PLAYER, createBox(1, 2));
	}

	private static BoundingBox createBox(double width, double height) {
		double w = width / 2;
		double h = height / 2;
		return new BoundingBox(-w, -h, -w, w, h, w);
	}

	protected ProjectileInstance(Projectile settings, FightData owner) {
		this(settings, owner, owner.getEntity().getLocation().add(0, 1.5, 0),
				owner.getEntity().getLocation().getDirection());
	}

	protected ProjectileInstance(Projectile settings, FightData owner, Location origin, Vector direction) {
		super(origin);
		this.inst = owner.getInstance();
		this.owner = owner;
		this.settings = settings;
		this.meta = new DamageMeta(owner, DamageOrigin.PROJECTILE).setProjectileInstance(this);
		meta.addOrigin(DamageOrigin.NORMAL);

		v = direction.clone().add(new Vector(0, settings.getArc(), 0)).normalize()
				.rotateAroundY(Math.toRadians(settings.getRotation()));
		if (settings.initialY() != 0)
			origin.add(0, settings.initialY(), 0);

		// Slow projectile, no interpolation needed
		if (settings.getWidth() > settings.getBlocksPerTick() * settings.getTickSpeed()) {
			v = v.multiply(settings.getBlocksPerTick() * settings.getTickSpeed());
			interpolationPoints = 1;
		}
		// Fast projectile, need interpolation
		else {
			v = v.multiply(settings.getWidth());
			interpolationPoints = (int) (settings.getBlocksPerTick() / settings.getWidth()) + 1;
		}
		distancePerPoint = v.length();
		loc = origin.clone().add(v.clone().multiply(0.5)); // Start slightly offset forward to avoid hitting behind
		bounds = BoundingBox.of(loc, settings.getWidth(), settings.getHeight(), settings.getWidth());
		// Loose bounds, this is because current bounds don't check for libsdisguises so
		// we need a bigger bounds that is more lenient
		// Ex: Baby zombie disguised as spider, bounds won't hit the spider's legs, but
		// bigBounds will
		bigBounds = BoundingBox.of(loc, settings.getWidth() + 2, settings.getHeight() + 2, settings.getWidth() + 2);

		// Home on enemies in front of the player
		if (settings.getHoming() != 0) {
			LivingEntity le = owner.getEntity();
			homingTarget = TargetHelper.getEntitiesInCone(le,
					TargetProperties.cone(90, settings.getMaxRange() + 5, false, TargetType.ENEMY)).peekFirst();
		}

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

	public void addDamageSlice(DamageSlice slice) {
		meta.addDamageSlice(slice);
	}

	// Sometimes useful for non-weapon projectiles like leading knife
	public void applyProperties(PlayerFightData data, Equipment eq) {
		EquipmentProperties props = eq.getProperties();
		meta.addDamageSlice(new DamageSlice(data, props.get(PropertyType.DAMAGE), props.getType()));
		meta.setKnockback(props.get(PropertyType.KNOCKBACK));
	}

	public void applyWeapon(PlayerFightData data, Equipment weapon) {
		EquipmentProperties props = weapon.getProperties();
		meta.addDamageSlice(new DamageSlice(data, props.get(PropertyType.DAMAGE), props.getType()));
		meta.setKnockback(props.get(PropertyType.KNOCKBACK));
		meta.isBasicAttack(weapon, true);
	}

	public void applyBowAndAmmo(PlayerFightData data, Bow bow, AmmunitionInstance ammo) {
		EquipmentProperties ammoProps = ammo.getProperties();
		double dmg = ammoProps.get(PropertyType.DAMAGE) + bow.getProperties().get(PropertyType.DAMAGE);
		meta.addDamageSlice(new DamageSlice(data, dmg, ammoProps.getType()));
		meta.setKnockback(bow.getProperties().get(PropertyType.KNOCKBACK) + ammo.getProperties().get(PropertyType.KNOCKBACK));
		meta.isBasicAttack(bow, true);
	}

	// Useful for applying miscellaneous properties that you don't want to show up in descriptions, like with Rapid Fire
	public void applyAmmo(PlayerFightData data, EquipmentProperties props, AmmunitionInstance ammo) {
		EquipmentProperties ammoProps = ammo.getProperties();
		double dmg = ammoProps.get(PropertyType.DAMAGE) + props.get(PropertyType.DAMAGE);
		meta.addDamageSlice(new DamageSlice(data, dmg, ammoProps.getType()));
		meta.setKnockback(
				props.get(PropertyType.KNOCKBACK) + ammo.getProperties().get(PropertyType.KNOCKBACK));
	}

	@Override
	public Projectile getParent() {
		return this.settings;
	}

	public int getTick() {
		return tick;
	}

	public void setHomingTarget(LivingEntity target) {
		this.homingTarget = target;
	}

	// Used to retain metadata within projectile
	public ActionMeta getActionMeta() {
		return am;
	}

	// True to cancel runnable
	private boolean tick() {
		tick++; // Has to be at the start since so many things end the method early
		for (int i = 0; i < interpolationPoints; i++) {
			// Check for collision with shields
			if (!settings.isIgnoreBarriers()) {
				for (Barrier b : inst.getEnemyBarriers().values()) {
					if (b.collides(loc)) {
						numHit++;
						DamageMeta clone = meta.clone();
						settings.onHit(FightInstance.getFightData(b.getOwner().getUniqueId()), b, clone, this);
						damageProjectile(b.getOwner(), clone, b);
						Player p = owner.getEntity() instanceof Player ? (Player) owner.getEntity() : null;
						Sounds.block.play(p, loc);
						return true;
					}
				}
			}

			// Check for collision with mobs
			if (!settings.isIgnoreEntities()) {
				for (Entity ent : loc.getWorld().getNearbyEntities(bigBounds)) {
					if (ent instanceof Player || ent.getType() == EntityType.ARMOR_STAND || ent instanceof Display)
						continue;
					if (!(ent instanceof LivingEntity))
						continue;

					// Check actual bounding box for disguised entity if applicable
					if (DisguiseAPI.isDisguised(ent)) {
						EntityType type = DisguiseAPI.getDisguise(ent).getType().getEntityType();
						BoundingBox disgBox = ent.getBoundingBox();
						if (!entityBounds.containsKey(type)) {
							Bukkit.getLogger().warning("NeoRogue doesn't have an appropriate bounding box for " + type);
						} else {
							disgBox = entityBounds.get(type).clone().shift(ent.getBoundingBox().getCenter());
						}
						if (!disgBox.overlaps(bounds))
							continue;
					} else {
						if (!ent.getBoundingBox().overlaps(bounds))
							continue;
					}

					UUID uuid = ent.getUniqueId();
					FightData hit = FightInstance.getFightData(uuid);
					if (hit == null)
						continue;
					if (targetsHit.contains(uuid))
						continue;
					targetsHit.add(uuid);

					// Make sure to never use the same damage meta twice
					DamageMeta clone = meta.clone();
					settings.onHit(hit, null, clone, this);
					for (HitAction act : hitActions) {
						act.onHit(hit, null, clone, this);
					}
					damageProjectile((LivingEntity) ent, clone, null);
					numHit++;

					int limit = settings.getPierceLimit() + pierceMod;
					if (limit != -1 && numHit > limit)
						return true;
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

			// Homing
			if (settings.getHoming() != 0 && homingTarget != null) {
				Vector between = homingTarget.getEyeLocation().toVector().subtract(loc.toVector()).normalize();
				v = v.add(between.multiply(settings.getHoming())).normalize().multiply(distancePerPoint);
			}

			settings.onTick(this, i);
			loc.add(v);
			bounds.shift(v);
			bigBounds.shift(v);
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

	private void damageProjectile(LivingEntity target, DamageMeta meta, Barrier hitBarrier) {
		meta.addDamageBuffLists(buffs); // Add projectile buffs
		// This is here for mob barriers blocking projectiles,
		// user barriers blocking projectiles are handled in damagemeta
		if (hitBarrier != null) {
			meta.addDefenseBuffLists(hitBarrier.getBuffLists());
		}
		FightInstance.dealDamage(meta, target);
	}

	public void cancel() {
		task.cancel();
	}

	public Vector getVelocity() {
		return v;
	}

	public HashMap<DamageBuffType, BuffList> getBuffLists() {
		return buffs;
	}

	public void addBuff(DamageBuffType type, Buff b) {
		BuffList list = buffs.getOrDefault(type, new BuffList());
		list.add(b);
		buffs.put(type, list);
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
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj);
	}

	public void addMaxRange(int range) {
		this.maxRangeMod += range;
	}

	public void addPierce(int pierce) {
		if (settings.getPierceLimit() == -1)
			return;
		this.pierceMod += pierce;
	}
}
