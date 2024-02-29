package me.neoblade298.neorogue.session.fight;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

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
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedHealthDamageEvent;

public class DamageMeta {
	private FightData owner;
	private boolean hitBarrier, isSecondary;
	private LinkedList<DamageSlice> slices = new  LinkedList<DamageSlice>();
	private HashMap<BuffType, LinkedList<BuffMeta>> damageBuffs = new HashMap<BuffType, LinkedList<BuffMeta>>(),
			defenseBuffs = new HashMap<BuffType, LinkedList<BuffMeta>>();
	private DamageMeta returnDamage;
	
	public DamageMeta(FightData data) {
		this.owner = data;
		addBuffs(owner.getBuffs(true), BuffOrigin.NORMAL, true);
	}
	
	public DamageMeta(FightData data, double damage, DamageType type) {
		this(data);
		this.slices.add(new DamageSlice(data.getUniqueId(), damage, type));
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
		this.hitBarrier = original.hitBarrier;
		this.isSecondary = original.isSecondary;
 		
 		// These are deep clones
		this.damageBuffs = cloneBuffMap(original.damageBuffs);
		this.defenseBuffs = cloneBuffMap(original.defenseBuffs);
	}
	
	private HashMap<BuffType, LinkedList<BuffMeta>> cloneBuffMap(HashMap<BuffType, LinkedList<BuffMeta>> map) {
		HashMap<BuffType, LinkedList<BuffMeta>> clone = new HashMap<BuffType, LinkedList<BuffMeta>>();
		for (Entry<BuffType, LinkedList<BuffMeta>> ent : map.entrySet()) {
			clone.put(ent.getKey(), cloneBuffList(ent.getValue()));
		}
		return clone;
	}
	
	private LinkedList<BuffMeta> cloneBuffList(LinkedList<BuffMeta> list) {
		LinkedList<BuffMeta> clone = new LinkedList<BuffMeta>();
		for (BuffMeta meta : list) {
			clone.add(meta.clone());
		}
		return clone;
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
	
	public void isSecondary(boolean isSecondary) {
		this.isSecondary = isSecondary;
	}
	
	public void setHitBarrier(boolean hitBarrier) {
		this.hitBarrier = hitBarrier;
	}
	
	public void addDamageSlice(DamageSlice slice) {
		this.slices.add(slice);
	}
	
	public void addBuffs(HashMap<BuffType, Buff> buffs, BuffOrigin origin, boolean damageBuff) {
		for (Entry<BuffType, Buff> buff : buffs.entrySet()) {
			addBuff(buff.getKey(), buff.getValue(), origin, damageBuff);
		}
	}
	
	public void addBuff(BuffType type, Buff b, BuffOrigin origin, boolean damageBuff) {
		HashMap<BuffType, LinkedList<BuffMeta>> buffs = damageBuff ? damageBuffs : defenseBuffs;
		LinkedList<BuffMeta> list = buffs.getOrDefault(type, new LinkedList<BuffMeta>());
		list.add(new BuffMeta(b, origin));
		buffs.putIfAbsent(type, list);
	}
	
	private boolean containsType(BuffType type) {
		for (DamageSlice slice : slices) {
			for (BuffType bt : slice.getType().getBuffTypes()) { 
				if (bt == type) return true;
			}
		}
		return false;
	}
	
	public void dealDamage(LivingEntity target) {
		if (slices.isEmpty()) return;
		FightData recipient = FightInstance.getFightData(target.getUniqueId());
		LivingEntity damager = owner.getEntity();
		addBuffs(recipient.getBuffs(false), BuffOrigin.NORMAL, false);
		double damage = 0;
		double ignoreShieldsDamage = 0;
		returnDamage = new DamageMeta(recipient);
		returnDamage.isSecondary = true;
		
		// See if any of our effects cancel damage first
		if (recipient instanceof PlayerFightData) {
			PlayerFightData pdata = (PlayerFightData) recipient;
			ReceivedDamageEvent ev = new ReceivedDamageEvent(damager, this);
			if (pdata.runActions(pdata, Trigger.RECEIVED_DAMAGE, ev)) {
				slices.clear();
			}
		}
		
		// Reduce damage from barriers, used only for players blocking projectiles
		// For mobs blocking projectiles, go to damageProjectile
		if (hitBarrier && recipient.getBarrier() != null) {
			addBuffs(recipient.getBarrier().getBuffs(), BuffOrigin.BARRIER, false);
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
				addBuffs(insanityBuffs, BuffOrigin.STATUS, true);
			}

			if (recipient.hasStatus(StatusType.SANCTIFIED)) {
				Status s = recipient.getStatus(StatusType.SANCTIFIED);
				int stacks = s.getStacks();
				for (Entry<UUID, Integer> slice : recipient.getStatus(StatusType.SANCTIFIED).getSlices().getSliceOwners().entrySet()) {
					FightInstance.giveHeal(Bukkit.getPlayer(slice.getKey()), stacks * 0.2, damager);
				}
				s.apply(owner.getUniqueId(), (int) (-stacks * 0.1), -1);
			}

			if (recipient.hasStatus(StatusType.SANCTIFIED)) {
				Status status = recipient.getStatus(StatusType.SANCTIFIED);
				int stacks = status.getStacks();
				int toRemove = (int) (stacks * 0.25);
				status.apply(owner.getUniqueId(), -toRemove, 0); // Remove 25% of stacks
				slices.add(new DamageSlice(recipient.getUniqueId(), toRemove, DamageType.LIGHT));
				owner.addHealth(toRemove);
			}
			
			if (owner.hasStatus(StatusType.FROST) && containsType(BuffType.MAGICAL)) {
				Status status = owner.getStatus(StatusType.FROST);
				int stacks = status.getStacks();
				int toRemove = (int) (-stacks * 0.1);
				status.apply(owner.getUniqueId(), toRemove, 0); // Remove 10% of frost stacks
				returnDamage.addDamageSlice(new DamageSlice(recipient.getUniqueId(), toRemove, DamageType.ICE));
			}

			if (owner.hasStatus(StatusType.CONCUSSED) && containsType(BuffType.PHYSICAL)) {
				Status status = owner.getStatus(StatusType.CONCUSSED);
				int stacks = status.getStacks();
				int toRemove = (int) (-stacks * 0.25);
				status.apply(owner.getUniqueId(), toRemove, 0); // Remove 10% of earth stacks
				returnDamage.addDamageSlice(new DamageSlice(recipient.getUniqueId(), toRemove, DamageType.EARTHEN));
			}
		}
		
		// Calculate buffs for every slice of damage
		for (DamageSlice slice : slices) {
			double increase = 0, mult = 0;
			for (BuffType bt : slice.getType().getBuffTypes()) {
				if (!damageBuffs.containsKey(bt)) continue;
				for (BuffMeta bm : damageBuffs.get(bt)) {
					Buff b = bm.buff;
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
			}

			for (BuffType bt : slice.getPostBuffType().getBuffTypes()) {
				if (!defenseBuffs.containsKey(bt)) continue;
				for (BuffMeta bm : defenseBuffs.get(bt)) {
					Buff b = bm.buff;
					increase -= b.getIncrease();
					mult -= b.getMultiplier();
					if (!(recipient instanceof PlayerFightData)) continue; // Don't need stats for non-player mitigation
					for (Entry<UUID, BuffSlice> ent : b.getSlices().entrySet()) {
						BuffSlice bs = ent.getValue();
						PlayerFightData buffOwner = FightInstance.getUserData(ent.getKey());
						if (buffOwner != null) {
							double amt = bs.getIncrease() + (bs.getMultiplier() * slice.getDamage());
							switch (bm.origin) {
							case BARRIER:
								buffOwner.getStats().addDamageBarriered(amt);
								break;
							default:
								buffOwner.getStats().addDamageMitigated(slice.getPostBuffType(), amt);
								break;
							}
						}
					}
				}
			}
			double sliceDamage = Math.max(0, (slice.getDamage() * (mult + 1)) + increase);
			if (owner instanceof PlayerFightData) {
				((PlayerFightData) owner).getStats().addDamageDealt(slice.getPostBuffType(), sliceDamage);
			}
			if (recipient instanceof PlayerFightData) {
				((PlayerFightData) recipient).getStats().addDamageTaken(slice.getPostBuffType(), sliceDamage);
			}
			
			if (!slice.isIgnoreShields()) {
				damage += sliceDamage;
			}
			else {
				ignoreShieldsDamage += sliceDamage;
			}

			// Return damage
			if (recipient.hasStatus(StatusType.THORNS) && slice.getPostBuffType().containsBuffType(BuffType.PHYSICAL)) {
				returnDamage.addDamageSlice(new DamageSlice(recipient.getUniqueId(), recipient.getStatus(StatusType.THORNS).getStacks(), DamageType.THORNS));
			}
			if (recipient.hasStatus(StatusType.REFLECT) && slice.getPostBuffType().containsBuffType(BuffType.MAGICAL)) {
				returnDamage.addDamageSlice(new DamageSlice(recipient.getUniqueId(), recipient.getStatus(StatusType.REFLECT).getStacks(), DamageType.REFLECT));
			}
		}
		
		// Threat
		if (NeoRogue.mythicApi.isMythicMob(target)) {
			NeoRogue.mythicApi.addThreat(target, owner.getEntity(), damage + ignoreShieldsDamage);
		}

		// Calculate damage to shields
		if (recipient.getShields() != null && !recipient.getShields().isEmpty()) {
			ShieldHolder shields = recipient.getShields();
			damage = Math.max(0, shields.useShields(damage));
		}
		
		// trigger received health damage
		if (recipient instanceof PlayerFightData && (damage > 0 || ignoreShieldsDamage > 0)) {
			PlayerFightData pdata = (PlayerFightData) recipient;
			ReceivedHealthDamageEvent ev = new ReceivedHealthDamageEvent(damager, this);
			if (pdata.runActions(pdata, Trigger.RECEIVED_HEALTH_DAMAGE, ev)) {
				damage = 0;
				ignoreShieldsDamage = 0;
			}
		}
		
		final double finalDamage = damage + ignoreShieldsDamage + target.getAbsorptionAmount();
		if (finalDamage > target.getAbsorptionAmount()) {
			target.damage(finalDamage);
			if (!(target instanceof Player)) {
				NeoRogue.mythicApi.castSkill(target, "UpdateHealthbar");
			}
			else {
				PlayerFightData data = FightInstance.getUserData(target.getUniqueId());
				if (data == null) return;
				data.getInstance().cancelRevives((Player) target);
				if (data.shields.getAmount() > 0 && ignoreShieldsDamage > 0) data.shields.update();
			}
		}
		else {
			target.damage(0.1);
		}
		
		// Return damage
		FightInstance.dealDamage(returnDamage, owner.getEntity());
	}
	
	public DamageMeta getReturnDamage() {
		return returnDamage;
	}
	
	private class BuffMeta {
		protected Buff buff;
		protected BuffOrigin origin;
		public BuffMeta(Buff buff, BuffOrigin origin) {
			super();
			this.buff = buff;
			this.origin = origin;
		}
		
		public BuffMeta clone() {
			return new BuffMeta(buff.clone(), origin);
		}
	}
	
	public static enum BuffOrigin {
		BARRIER,
		STATUS,
		SHIELD,
		PROJECTILE,
		NORMAL;
	}
}
