package me.neoblade298.neorogue.session.fight;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffSlice;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.KillEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedHealthDamageEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class DamageMeta {
	private static HashSet<EntityType> armoredEntities = new HashSet<EntityType>();
	
	private FightData owner;
	private boolean hitBarrier, isSecondary;
	private DamageOrigin origin;
	private LinkedList<DamageSlice> slices = new  LinkedList<DamageSlice>();
	private HashMap<BuffType, LinkedList<BuffMeta>> damageBuffs = new HashMap<BuffType, LinkedList<BuffMeta>>(),
			defenseBuffs = new HashMap<BuffType, LinkedList<BuffMeta>>();
	private DamageMeta returnDamage;
	
	static {
		armoredEntities.add(EntityType.ZOMBIE);
		armoredEntities.add(EntityType.HUSK);
		armoredEntities.add(EntityType.SKELETON);
		armoredEntities.add(EntityType.STRAY);
		armoredEntities.add(EntityType.WITHER_SKELETON);
		armoredEntities.add(EntityType.DROWNED);
		armoredEntities.add(EntityType.GUARDIAN);
	}
	
	public DamageMeta(FightData data) {
		this.owner = data;
		addBuffs(owner.getBuffs(true), BuffOrigin.NORMAL, true);
	}
	
	public DamageMeta(FightData data, double damage, DamageType type) {
		this(data);
		this.slices.add(new DamageSlice(data, damage, type));
	}
	
	public DamageMeta(FightData data, double damage, DamageType type, DamageOrigin origin) {
		this(data);
		this.slices.add(new DamageSlice(data, damage, type));
		this.origin = origin;
	}
	
	public DamageMeta(FightData data, double baseDamage, DamageType type, DamageOrigin origin, boolean hitBarrier, boolean isSecondary) {
		this(data, baseDamage, type);
		this.origin = origin;
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

	public DamageOrigin getOrigin() {
		return origin;
	}
	
	public void addDamageSlice(DamageSlice slice) {
		for (DamageSlice ds : slices) {
			if (ds.isSimilar(slice)) {
				ds.add(slice);
				return;
			}
		}
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
	
	public boolean containsType(BuffType type) {
		for (DamageSlice slice : slices) {
			for (BuffType bt : slice.getType().getBuffTypes()) { 
				if (bt == type) return true;
			}
		}
		return false;
	}
	
	public double dealDamage(LivingEntity target) {
		if (slices.isEmpty()) return 0;
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
			ReceivedDamageEvent ev = new ReceivedDamageEvent(owner, this);
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
				for (Entry<FightData, Integer> ent : recipient.getStatus(StatusType.BURN).getSlices().getSliceOwners().entrySet()) {
					slices.add(new DamageSlice(ent.getKey(), ent.getValue() * 0.1, DamageType.FIRE));
				}
			}

			if (owner.hasStatus(StatusType.SANCTIFIED)) {
				Status status = owner.getStatus(StatusType.SANCTIFIED);
				int stacks = status.getStacks();
				int toRemove = (int) (stacks * 0.25);
				status.apply(owner, -toRemove, 0); // Remove 25% of stacks
				slices.add(new DamageSlice(owner, toRemove, DamageType.LIGHT));
				recipient.addHealth(toRemove);
			}
			
			if (owner.hasStatus(StatusType.FROST) && containsType(BuffType.MAGICAL)) {
				Status status = owner.getStatus(StatusType.FROST);
				int stacks = status.getStacks();
				int toRemove = (int) (-stacks * 0.2);
				status.apply(owner, toRemove, 0);
				returnDamage.addDamageSlice(new DamageSlice(recipient, toRemove, DamageType.ICE));
			}

			if (owner.hasStatus(StatusType.CONCUSSED) && containsType(BuffType.PHYSICAL)) {
				Status status = owner.getStatus(StatusType.CONCUSSED);
				int stacks = status.getStacks();
				int toRemove = (int) (-stacks * 0.25);
				status.apply(owner, toRemove, 0);
				returnDamage.addDamageSlice(new DamageSlice(recipient, toRemove, DamageType.EARTHEN));
			}
		}
		
		// Calculate buffs for every slice of damage
		boolean evading = recipient.hasStatus(StatusType.EVADE) && 
				(slices.isEmpty() ? false : slices.getFirst().getPostBuffType().containsBuffType(BuffType.GENERAL));
		if (evading) {
			recipient.getStatus(StatusType.EVADE).apply(recipient, -1, -1);
		}
		for (DamageSlice slice : slices) {
			double increase = 0, mult = 1;
			for (BuffType bt : slice.getType().getBuffTypes()) {
				if (!damageBuffs.containsKey(bt)) continue;
				for (BuffMeta bm : damageBuffs.get(bt)) {
					Buff b = bm.buff;
					increase += b.getIncrease();
					mult += b.getMultiplier();
					if (!(owner instanceof PlayerFightData)) continue; // Don't need stats for non-player damager
					for (Entry<FightData, BuffSlice> ent : b.getSlices().entrySet()) {
						BuffSlice bs = ent.getValue();
						if (ent.getKey() instanceof PlayerFightData) {
							((PlayerFightData) ent.getKey()).getStats().addDamageBuffed(slice.getType(), bs.getIncrease() + (bs.getMultiplier() * slice.getDamage()));
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
					for (Entry<FightData, BuffSlice> ent : b.getSlices().entrySet()) {
						BuffSlice bs = ent.getValue();
						if (ent.getKey() instanceof PlayerFightData) {
							PlayerFightData buffOwner = (PlayerFightData) ent.getKey();
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
			
			double sliceDamage = Math.max(0, (slice.getDamage() * mult) + increase);
			// If the first slice isn't a status, evade it
			if (evading) {
				if (recipient.getEntity().getType() == EntityType.PLAYER) Sounds.attackSweep.play((Player) recipient.getEntity(), recipient.getEntity());
				PlayerFightData pl = (PlayerFightData) recipient; // Only players can have evade status
				if (sliceDamage > pl.getStamina()) {
					sliceDamage -= pl.getStamina();
					pl.getStats().addDamageMitigated(slice.getPostBuffType(), pl.getStamina());
					pl.setStamina(0);
				}
				else {
					pl.addStamina(-sliceDamage);
					pl.getStats().addDamageMitigated(slice.getPostBuffType(), sliceDamage);
					sliceDamage = 0;
				}
			}

			// Handle injury
			while (recipient.hasStatus(StatusType.INJURY) && sliceDamage > 0) {
				Status injury = recipient.getStatus(StatusType.INJURY);
				int stacks = injury.getStacks();
				if (stacks * 0.2 >= sliceDamage) {
					int toRemove = (int) (sliceDamage / 0.2);
					sliceDamage = 0;
					injury.apply(owner, -toRemove, -1);
				}
				else {
					sliceDamage -= stacks * 0.2;
					injury.apply(owner, -stacks, -1);
				}
			}
			
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
				returnDamage.addDamageSlice(new DamageSlice(recipient, recipient.getStatus(StatusType.THORNS).getStacks(), DamageType.THORNS));
			}
			if (recipient.hasStatus(StatusType.REFLECT) && slice.getPostBuffType().containsBuffType(BuffType.MAGICAL)) {
				returnDamage.addDamageSlice(new DamageSlice(recipient, recipient.getStatus(StatusType.REFLECT).getStacks(), DamageType.REFLECT));
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
			ReceivedHealthDamageEvent ev = new ReceivedHealthDamageEvent(damager, this, damage, ignoreShieldsDamage);
			if (pdata.runActions(pdata, Trigger.RECEIVED_HEALTH_DAMAGE, ev)) {
				damage = 0;
				ignoreShieldsDamage = 0;
			}
		}
		double finalDamage = damage + ignoreShieldsDamage + target.getAbsorptionAmount();
		if (damage + ignoreShieldsDamage > 0) {
			
			// Mobs shouldn't have a source of damage because they'll infinitely re-trigger ~OnAttack
			// Players must have a source of damage to get credit for kills, otherwise mobs that suicide give points
			if (owner instanceof PlayerFightData) {
				if (armoredEntities.contains(target.getType())) finalDamage *= 1.09; // To deal with minecraft vanilla armor, rounded up for inconsistency
				FightInstance.trigger((Player) owner.getEntity(), Trigger.DEALT_DAMAGE, new DealtDamageEvent(this, target, damage, ignoreShieldsDamage));
				target.damage(finalDamage, owner.getEntity());
			}
			else {
				target.damage(finalDamage);
			}
			
			if (target.getHealth() <= 0 && owner instanceof PlayerFightData) {
				FightInstance.trigger((Player) owner.getEntity(), Trigger.KILL, new KillEvent(target));
			}
			if (!(target instanceof Player)) {
				recipient.updateDisplayName();
				recipient.getInstance().createIndicator(Component.text((int) (damage + ignoreShieldsDamage), NamedTextColor.RED), target);
			}
			else {
				PlayerFightData data = FightInstance.getUserData(target.getUniqueId());
				if (data == null) return damage + ignoreShieldsDamage; // Should hopefully never happen
				data.getInstance().cancelRevives((Player) target);
				if (data.shields.getAmount() > 0 && ignoreShieldsDamage > 0) data.shields.update();
				data.updateActionBar();
			}
		}
		// Only do damage if we haven't canceled the damage
		else if (!slices.isEmpty()) {
			target.damage(0.001);
		}
		
		// Return damage
		FightInstance.dealDamage(returnDamage, owner.getEntity());
		return damage + ignoreShieldsDamage;
	}
	
	public DamageMeta getReturnDamage() {
		return returnDamage;
	}
	
	public DamageSlice getPrimarySlice() {
		return slices.getFirst();
	}
	
	public LinkedList<DamageSlice> getSlices() {
		return slices;
	}
	
	public boolean containsType(DamageType type) {
		for (DamageSlice slice : slices) {
			if (slice.getPostBuffType() == type) return true;
		}
		return false;
	}

	@Override
	public String toString() {
		String str = "";
		for (DamageSlice slice : slices) {
			str += slice.toString() + ", ";
		}
		return str;
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
		INITIAL_VELOCITY, // Multiplier of initial projectile velocity
		NORMAL;
	}

	public static enum DamageOrigin {
		NORMAL,
		TRAP;
	}
}
