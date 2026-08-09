package me.neoblade298.neorogue.equipment.mechanics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;

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

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffList;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;

public class OrbitalInstance {
	private static final HashSet<OrbitalInstance> activeInstances = new HashSet<OrbitalInstance>();

	private final Orbital settings;
	private final FightData owner;
	private final FightInstance inst;
	private final HashSet<UUID> targetsHit = new HashSet<UUID>();
	private final HashMap<DamageBuffType, BuffList> buffs = new HashMap<DamageBuffType, BuffList>();
	private final ArrayList<HitBlockAction> hitBlockActions = new ArrayList<HitBlockAction>();
	private final ArrayList<HitAction> hitActions = new ArrayList<HitAction>();
	private final ActionMeta actionMeta = new ActionMeta();
	private final DamageMeta meta;
	private final int durationTicks;
	private BukkitTask task;
	private Location center, loc;
	private BoundingBox bounds, bigBounds;
	private double angle;
	private int tick, numHit, pierceMod;
	private String tag;

	protected OrbitalInstance(Orbital settings, FightData owner) {
		this.settings = settings;
		this.owner = owner;
		this.inst = owner.getInstance();
		this.durationTicks = Math.max(1, (int) Math.round(settings.getDuration() * 20));
		this.angle = Math.toRadians(settings.getInitialRotation());
		this.center = getCenter();
		this.loc = pointOnRing(center, angle);
		this.bounds = BoundingBox.of(loc, settings.getWidth(), settings.getHeight(), settings.getWidth());
		this.bigBounds = BoundingBox.of(loc, settings.getWidth() + 2, settings.getHeight() + 2,
				settings.getWidth() + 2);
		this.meta = new DamageMeta(owner);

		task = new BukkitRunnable() {
			@Override
			public void run() {
				if (tick()) {
					activeInstances.remove(OrbitalInstance.this);
					cancel();
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 0L, 1L);
		owner.addTask(task);
		activeInstances.add(this);
	}

	private boolean tick() {
		LivingEntity entity = owner.getEntity();
		if (entity == null || !entity.isValid()) return true;
		if (tick >= durationTicks) {
			settings.onFizzle(this);
			return true;
		}

		Location nextCenter = getCenter();
		if (!center.getWorld().equals(nextCenter.getWorld())) return true;
		double angleStep = settings.getRotationsPerSecond() * Math.PI * 2 / 20;
		double pathLength = Math.abs(settings.getRingRadius() * angleStep) + center.distance(nextCenter);
		int interpolationPoints = Math.max(1,
				(int) Math.ceil(pathLength / Math.max(settings.getWidth(), 0.1)));

		for (int i = 0; i < interpolationPoints; i++) {
			double progress = (double) i / interpolationPoints;
			Location interpolatedCenter = center.clone().add(nextCenter.toVector().subtract(center.toVector()).multiply(progress));
			moveTo(pointOnRing(interpolatedCenter, angle + angleStep * progress));
			if (collide()) return true;
			settings.onTick(this, i);
		}

		angle += angleStep;
		center = nextCenter;
		moveTo(pointOnRing(center, angle));
		tick++;
		return false;
	}

	private boolean collide() {
		if (!settings.isIgnoreBarriers()) {
			for (Barrier barrier : inst.getEnemyBarriers().values()) {
				if (!barrier.collides(loc)) continue;
				numHit++;
				DamageMeta clone = meta.clone();
				FightData hit = FightInstance.getFightData(barrier.getOwner().getUniqueId());
				settings.onHit(hit, barrier, clone, this);
				for (HitAction action : hitActions) action.onHit(hit, barrier, clone, this);
				damage(barrier.getOwner(), clone, barrier);
				Player player = owner.getEntity() instanceof Player ? (Player) owner.getEntity() : null;
				Sounds.block.play(player, loc);
				return true;
			}
		}

		if (!settings.isIgnoreEntities()) {
			for (Entity entity : loc.getWorld().getNearbyEntities(bigBounds)) {
				if (!(entity instanceof LivingEntity) || entity instanceof Player || entity instanceof Display
						|| entity.getType() == EntityType.ARMOR_STAND || !entity.getBoundingBox().overlaps(bounds)) continue;
				FightData hit = FightInstance.getFightData(entity.getUniqueId());
				if (hit == null || !targetsHit.add(entity.getUniqueId())) continue;

				DamageMeta clone = meta.clone();
				settings.onHit(hit, null, clone, this);
				for (HitAction action : hitActions) action.onHit(hit, null, clone, this);
				damage((LivingEntity) entity, clone, null);
				numHit++;
				int limit = settings.getPierceLimit() + pierceMod;
				if (limit != -1 && numHit > limit) return true;
			}
		}

		if (!settings.isIgnoreBlocks()) {
			Block block = loc.getBlock();
			if (!block.isPassable()) {
				for (BoundingBox hitbox : block.getCollisionShape().getBoundingBoxes()) {
					hitbox.shift(block.getLocation());
					if (!bounds.overlaps(hitbox)) continue;
					settings.onHitBlock(this, block);
					for (HitBlockAction action : hitBlockActions) action.onHitBlock(this, block);
					return true;
				}
			}
		}
		return false;
	}

	private Location getCenter() {
		return owner.getEntity().getLocation().add(0, settings.getInitialY(), 0);
	}

	private Location pointOnRing(Location center, double angle) {
		return center.clone().add(Math.cos(angle) * settings.getRingRadius(), 0,
				Math.sin(angle) * settings.getRingRadius());
	}

	private void moveTo(Location destination) {
		Vector movement = destination.toVector().subtract(loc.toVector());
		loc = destination;
		bounds.shift(movement);
		bigBounds.shift(movement);
	}

	private void damage(LivingEntity target, DamageMeta damageMeta, Barrier hitBarrier) {
		damageMeta.addDamageBuffLists(buffs);
		if (hitBarrier != null) damageMeta.addDefenseBuffLists(hitBarrier.getBuffLists());
		FightInstance.dealDamage(damageMeta, target);
	}

	public void applyProperties(PlayerFightData data, Equipment equipment, int slot) {
		EquipmentProperties properties = equipment.getProperties();
		meta.addDamageSlice(new DamageSlice(data, properties.get(PropertyType.DAMAGE), properties.getType(),
				DamageStatTracker.of(equipment.getId() + slot, equipment)));
		meta.setKnockback(properties.get(PropertyType.KNOCKBACK));
	}

	public void applyWeapon(PlayerFightData data, Equipment weapon, int slot) {
		applyProperties(data, weapon, slot);
		meta.isBasicAttack(weapon, true);
	}

	public void addDamageSlice(DamageSlice slice) {
		meta.addDamageSlice(slice);
	}

	public void addHitBlockAction(HitBlockAction action) {
		hitBlockActions.add(action);
	}

	public void addHitAction(HitAction action) {
		hitActions.add(action);
	}

	public void cancel() {
		activeInstances.remove(this);
		task.cancel();
	}

	public static void cancelAll(FightInstance inst) {
		Iterator<OrbitalInstance> iterator = activeInstances.iterator();
		while (iterator.hasNext()) {
			OrbitalInstance orbital = iterator.next();
			if (orbital.inst != inst) continue;
			iterator.remove();
			orbital.task.cancel();
		}
	}

	public Orbital getParent() {
		return settings;
	}

	public FightData getOwner() {
		return owner;
	}

	public Location getLocation() {
		return loc;
	}

	public DamageMeta getMeta() {
		return meta;
	}

	public int getTick() {
		return tick;
	}

	public int getNumHit() {
		return numHit;
	}

	public ActionMeta getActionMeta() {
		return actionMeta;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getTag() {
		return tag;
	}

	public HashMap<DamageBuffType, BuffList> getBuffLists() {
		return buffs;
	}

	public void addBuff(DamageBuffType type, Buff buff) {
		BuffList list = buffs.getOrDefault(type, new BuffList());
		list.add(buff);
		buffs.put(type, list);
	}

	public void addPierce(int pierce) {
		if (settings.getPierceLimit() != -1) pierceMod += pierce;
	}

	public interface HitBlockAction {
		public void onHitBlock(OrbitalInstance orbital, Block block);
	}

	public interface HitAction {
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, OrbitalInstance orbital);
	}
}