package me.neoblade298.neorogue.session.fight;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffSlice;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedDamageEvent;

public class DamageMeta {
	private FightData owner;
	private boolean hitBarrier, isSecondary;
	private LinkedList<DamageSlice> slices = new  LinkedList<DamageSlice>();
	private LinkedList<BuffMap> damageBuffs = new LinkedList<BuffMap>(),
			defenseBuffs = new LinkedList<BuffMap>();
	
	public DamageMeta(FightData data) {
		this.owner = data;
		this.damageBuffs.add(new BuffMap(owner.getBuffs(true), BuffMapOrigin.NORMAL));
	}
	
	public DamageMeta(FightData data, double damage, DamageType type) {
		this.owner = data;
		this.slices.add(new DamageSlice(data.getUniqueId(), damage, type));
		this.damageBuffs.add(new BuffMap(owner.getBuffs(true), BuffMapOrigin.NORMAL));
	}
	
	public DamageMeta(FightData data, double baseDamage, DamageType type, boolean hitBarrier, boolean isSecondary) {
		this(data, baseDamage, type);
		this.hitBarrier = hitBarrier;
		this.isSecondary = isSecondary;
	}
	
	private DamageMeta(DamageMeta original) {
		this.owner = original.owner;
		this.hitBarrier = original.hitBarrier;
 		this.slices = new LinkedList<DamageSlice>(original.slices);
 		// As it is now, these are not deep clones
		this.damageBuffs = new LinkedList<BuffMap>(original.damageBuffs);
		this.defenseBuffs = new LinkedList<BuffMap>(original.defenseBuffs);
	}
	
	public DamageMeta clone() {
		return new DamageMeta(this);
	}
	
	public FightData getOwner() {
		return owner;
	}

	public boolean hitBarrier() {
		return hitBarrier;
	}
	
	public boolean isSecondary() {
		return isSecondary;
	}
	
	public void setHitBarrier(boolean hitBarrier) {
		this.hitBarrier = hitBarrier;
	}
	
	public void addDamageSlice(DamageSlice slice) {
		this.slices.add(slice);
	}
	
	public void addDamageBuffs(HashMap<BuffType, Buff> buffs, BuffMapOrigin origin) {
		damageBuffs.add(new BuffMap(buffs, origin));
	}
	
	public void addDefenseBuffs(HashMap<BuffType, Buff> buffs, BuffMapOrigin origin) {
		defenseBuffs.add(new BuffMap(buffs, origin));
	}
	
	public void dealDamage(LivingEntity target) {
		FightData recipient = FightInstance.getFightData(target.getUniqueId());
		LivingEntity damager = owner.getEntity();
		addDefenseBuffs(recipient.getBuffs(false), BuffMapOrigin.NORMAL);
		double damage = 0;
		double ignoreShieldsDamage = 0;
		
		// See if any of our effects cancel damage first
		if (recipient instanceof PlayerFightData) {
			PlayerFightData pdata = (PlayerFightData) recipient;
			ReceivedDamageEvent ev = new ReceivedDamageEvent(damager, this);
			if (pdata.runActions(pdata, Trigger.RECEIVED_DAMAGE, ev)) {
				return;
			}
		}
		
		// Reduce damage from barriers, used only for players blocking projectiles
		// For mobs blocking projectiles, go to damageProjectile
		if (hitBarrier && recipient.getBarrier() != null) {
			addDefenseBuffs(recipient.getBarrier().getBuffs(), BuffMapOrigin.BARRIER);
		}

		// Status effects
		if (!isSecondary) {
			if (recipient.hasStatus(StatusType.BURN)) {
				for (Entry<UUID, Integer> ent : recipient.getStatus(StatusType.BURN).getSlices().getSliceOwners().entrySet()) {
					slices.add(new DamageSlice(ent.getKey(), ent.getValue() * 0.1, DamageType.FIRE));
				}
			}

			if (recipient.hasStatus(StatusType.ELECTRIFIED)) {
				TargetProperties tp = TargetProperties.radius(5, true, owner instanceof PlayerFightData ? TargetType.ENEMY : TargetType.ALLY);
				FightInstance.dealDamage(
						new DamageMeta(owner, recipient.getStatus(StatusType.ELECTRIFIED).getStacks() * 0.1, DamageType.LIGHTNING, false, true),
						TargetHelper.getEntitiesInRadius(recipient.getEntity(), tp));
			}

			if (owner.hasStatus(StatusType.INSANITY)) {
				HashMap<BuffType, Buff> insanityBuffs = new HashMap<BuffType, Buff>();
				int stacks = owner.getStatus(StatusType.INSANITY).getStacks();
				insanityBuffs.put(BuffType.MAGICAL, new Buff(owner.getUniqueId(), 0, stacks * 0.01));
				addDamageBuffs(insanityBuffs, BuffMapOrigin.STATUS);
			}

			if (recipient.hasStatus(StatusType.SANCTIFIED)) {
				int stacks = recipient.getStatus(StatusType.SANCTIFIED).getStacks();
				FightInstance.giveHeal(damager, stacks * 0.5, damager);
			}

			// Return damage
			DamageMeta returnDamage = new DamageMeta(recipient);
			returnDamage.isSecondary = true;
			if (recipient.hasStatus(StatusType.THORNS)) {
				returnDamage.addDamageSlice(new DamageSlice(recipient.getUniqueId(), recipient.getStatus(StatusType.THORNS).getStacks(), DamageType.THORNS));
			}
			
			if (owner.hasStatus(StatusType.FROST)) {
				HashMap<BuffType, Buff> frostBuffs = new HashMap<BuffType, Buff>();
				HashMap<UUID, BuffSlice> buffSlices = new HashMap<UUID, BuffSlice>();
				Status status = owner.getStatus(StatusType.FROST);
				int stacks = status.getStacks();
				// Transfer status slices into buff slices
				for (Entry<UUID, Integer> ent : status.getSlices().getSliceOwners().entrySet()) {
					BuffSlice bs = buffSlices.getOrDefault(ent.getKey(), new BuffSlice());
					bs.addMultiplier(ent.getValue() * 0.01);
					buffSlices.putIfAbsent(ent.getKey(), bs);
				}
				frostBuffs.put(BuffType.MAGICAL, new Buff(0, stacks * 0.01, buffSlices));
				addDefenseBuffs(frostBuffs, BuffMapOrigin.STATUS);
				int toRemove = (int) (-stacks * 0.1);
				status.apply(owner.getUniqueId(), toRemove, 0); // Remove 10% of frost stacks
				returnDamage.addDamageSlice(new DamageSlice(recipient.getUniqueId(), toRemove, DamageType.ICE));
			}

			if (owner.hasStatus(StatusType.CONCUSSED)) {
				HashMap<BuffType, Buff> concussedBuffs = new HashMap<BuffType, Buff>();
				HashMap<UUID, BuffSlice> buffSlices = new HashMap<UUID, BuffSlice>();
				Status status = owner.getStatus(StatusType.CONCUSSED);
				int stacks = status.getStacks();
				// Transfer status slices into buff slices
				for (Entry<UUID, Integer> ent : status.getSlices().getSliceOwners().entrySet()) {
					BuffSlice bs = buffSlices.getOrDefault(ent.getKey(), new BuffSlice());
					bs.addMultiplier(ent.getValue() * 0.01);
					buffSlices.putIfAbsent(ent.getKey(), bs);
				}
				concussedBuffs.put(BuffType.PHYSICAL, new Buff(0, stacks * 0.01, buffSlices));
				addDefenseBuffs(concussedBuffs, BuffMapOrigin.STATUS);
				int toRemove = (int) (-stacks * 0.1);
				status.apply(owner.getUniqueId(), toRemove, 0); // Remove 10% of earth stacks
				returnDamage.addDamageSlice(new DamageSlice(recipient.getUniqueId(), toRemove, DamageType.EARTH));
			}
			FightInstance.dealDamage(returnDamage, owner.getEntity());
		}
		
		// Calculate buffs for every slice of damage
		for (DamageSlice slice : slices) {
			double increase = 0, mult = 0;
			for (BuffType bt : slice.getType().getBuffTypes()) {
				for (BuffMap buffMap : damageBuffs) {
					HashMap<BuffType, Buff> buffs = buffMap.getBuffs();
					if (!buffs.containsKey(bt)) continue;
					Buff b = buffs.get(bt);
					increase += b.getIncrease();
					mult += b.getMultiplier();
					if (!(owner instanceof PlayerFightData)) continue; // Don't need stats for non-player damager
					for (Entry<UUID, BuffSlice> ent : b.getSlices().entrySet()) {
						BuffSlice bs = ent.getValue();
						PlayerFightData buffOwner = FightInstance.getUserData(ent.getKey());
						if (buffOwner != null) {
							buffOwner.getStats().addDamageBuffed(slice.getType(), bs.getIncrease() + (bs.getMultiplier() * slice.getDamage()));
						}
					}
				}

				for (BuffMap buffMap : defenseBuffs) {
					HashMap<BuffType, Buff> buffs = buffMap.getBuffs();
					if (!buffs.containsKey(bt)) continue;
					Buff b = buffs.get(bt);
					increase -= b.getIncrease();
					mult -= b.getMultiplier();
					if (!(recipient instanceof PlayerFightData)) continue; // Don't need stats for non-player mitigation
					for (Entry<UUID, BuffSlice> ent : b.getSlices().entrySet()) {
						BuffSlice bs = ent.getValue();
						PlayerFightData buffOwner = FightInstance.getUserData(ent.getKey());
						if (buffOwner != null) {
							double amt = bs.getIncrease() + (bs.getMultiplier() * slice.getDamage());
							switch (buffMap.getOrigin()) {
							case BARRIER:
								buffOwner.getStats().addDamageBarriered(amt);
								break;
							default:
								buffOwner.getStats().addDamageMitigated(slice.getType(), amt);
								break;
							}
						}
					}
				}
			}
			if (slice.isIgnoreShields()) {
				damage += (slice.getDamage() * (mult + 1)) + increase;
			}
			else {
				ignoreShieldsDamage += (slice.getDamage() * (mult + 1)) + increase;
			}
		}
		
		// Threat
		if (NeoRogue.mythicApi.isMythicMob(target)) {
			NeoRogue.mythicApi.addThreat(owner.getEntity(), (LivingEntity) target, damage + ignoreShieldsDamage);
		}

		// Calculate damage to shields
		if (recipient.getShields() != null && !recipient.getShields().isEmpty()) {
			ShieldHolder shields = recipient.getShields();
			damage = Math.max(0, shields.useShields(damage));
			
			// Update shield after if damage was dealt through shield
			if (damage > 0 || ignoreShieldsDamage > 0) {
				new BukkitRunnable() {
					public void run() {
						shields.update();
					}
				}.runTask(NeoRogue.inst());
			}
		}
		
		final double finalDamage = damage + ignoreShieldsDamage + target.getAbsorptionAmount();
		if (finalDamage >= 0) {
			target.damage(finalDamage, damager);
			if (!(target instanceof Player)) {
				NeoRogue.mythicApi.castSkill(target, "UpdateHealthbar");
			}
		}
		else {
			target.damage(0, damager);
		}
		
	}
	
	private class BuffMap {
		private HashMap<BuffType, Buff> buffs;
		private BuffMapOrigin origin;
		public BuffMap(HashMap<BuffType, Buff> buffs, BuffMapOrigin origin) {
			super();
			this.buffs = buffs;
			this.origin = origin;
		}
		public HashMap<BuffType, Buff> getBuffs() {
			return buffs;
		}
		public BuffMapOrigin getOrigin() {
			return origin;
		}
	}
	
	public static enum BuffMapOrigin {
		BARRIER,
		STATUS,
		SHIELD,
		PROJECTILE,
		NORMAL;
	}
}
