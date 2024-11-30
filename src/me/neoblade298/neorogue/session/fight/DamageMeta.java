package me.neoblade298.neorogue.session.fight;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.libraryaddict.disguise.DisguiseAPI;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.mechanics.IProjectileInstance;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffSlice;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealtDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedHealthDamageEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class DamageMeta {
	private static HashSet<EntityType> armoredEntities = new HashSet<EntityType>();
	
	private FightData owner;
	private boolean hitBarrier, isSecondary;
	private HashSet<DamageOrigin> origins = new HashSet<DamageOrigin>();
	private IProjectileInstance proj; // If the damage originated from projectile
	private LinkedList<DamageSlice> slices = new  LinkedList<DamageSlice>();
	private HashMap<DamageBuffType, LinkedList<Buff>> damageBuffs = new HashMap<DamageBuffType, LinkedList<Buff>>(),
			defenseBuffs = new HashMap<DamageBuffType, LinkedList<Buff>>();
	private DamageMeta returnDamage;
	
	static {
		armoredEntities.add(EntityType.ZOMBIE);
		armoredEntities.add(EntityType.HUSK);
		armoredEntities.add(EntityType.DROWNED);
	}
	
	public DamageMeta(FightData data) {
		this.owner = data;
	}
	
	public DamageMeta(FightData data, DamageOrigin origin) {
		this(data);
		this.origins.add(origin);
	}
	
	public DamageMeta(FightData data, double damage, DamageType type) {
		this(data);
		this.slices.add(new DamageSlice(data, damage, type));
	}
	
	public DamageMeta(FightData data, double damage, DamageType type, DamageOrigin origin) {
		this(data);
		this.slices.add(new DamageSlice(data, damage, type));
		this.origins.add(origin);
	}
	
	public DamageMeta(FightData data, double damage, DamageType type, DamageOrigin origin, IProjectileInstance proj) {
		this(data);
		this.slices.add(new DamageSlice(data, damage, type));
		this.origins.add(origin);
		this.proj = proj;
	}
	
	public DamageMeta(FightData data, double baseDamage, DamageType type, DamageOrigin origin, boolean hitBarrier, boolean isSecondary) {
		this(data, baseDamage, type);
		this.origins.add(origin);
		this.hitBarrier = hitBarrier;
		this.isSecondary = isSecondary;
	}
	
	private DamageMeta(DamageMeta original) {
		this.owner = original.owner;
		this.hitBarrier = original.hitBarrier;
 		this.slices = new LinkedList<DamageSlice>(original.slices);
		this.hitBarrier = original.hitBarrier;
		this.isSecondary = original.isSecondary;
		this.origins = original.origins;
		this.proj = original.proj;
 		
 		// These are deep clones
		this.damageBuffs = cloneBuffMap(original.damageBuffs);
		this.defenseBuffs = cloneBuffMap(original.defenseBuffs);
	}

	public void setOwner(FightData owner) {
		this.owner = owner;
	}

	public void setProjectileInstance(IProjectileInstance inst) {
		this.proj = inst;
	}
	
	private HashMap<DamageBuffType, LinkedList<Buff>> cloneBuffMap(HashMap<DamageBuffType, LinkedList<Buff>> map) {
		HashMap<DamageBuffType, LinkedList<Buff>> clone = new HashMap<DamageBuffType, LinkedList<Buff>>();
		for (Entry<DamageBuffType, LinkedList<Buff>> ent : map.entrySet()) {
			LinkedList<Buff> list = new LinkedList<Buff>();
			clone.put(ent.getKey(), list);
			for (Buff b : ent.getValue()) {
				list.add(b.clone());
			}
		}
		return clone;
	}

	public IProjectileInstance getProjectile() {
		return proj;
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

	public boolean hasOrigin(DamageOrigin origin) {
		return this.origins.contains(origin);
	}

	public void addOrigin(DamageOrigin origin) {
		this.origins.add(origin);
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
	
	public void addBuffs(HashMap<DamageBuffType, LinkedList<Buff>> buffs, boolean damageBuff) {
		for (Entry<DamageBuffType, LinkedList<Buff>> ent : buffs.entrySet()) {
			for (Buff b : ent.getValue()) {
				addBuff(ent.getKey(), b, damageBuff);
			}
		}
	}
	
	public void addBuff(DamageBuffType type, Buff b, boolean damageBuff) {
		HashMap<DamageBuffType, LinkedList<Buff>> buffs = damageBuff ? damageBuffs : defenseBuffs;
		LinkedList<Buff> list = buffs.getOrDefault(type, new LinkedList<Buff>());

		boolean found = false;
		for (Buff buff : list) {
			if (buff.isSimilar(b)) {
				buff.combineBuff(b);
				found = true;
				break;
			}
		}

		if (!found) {
			list.add(b);
		}
		buffs.putIfAbsent(type, list);
	}
	
	public double dealDamage(LivingEntity target) {
		if (slices.isEmpty()) return 0;
		if (target.getType() == EntityType.ARMOR_STAND) return 0;
		FightData recipient = FightInstance.getFightData(target.getUniqueId());
		LivingEntity damager = owner.getEntity();
		addBuffs(owner.getBuffs(true), true);
		addBuffs(recipient.getBuffs(false), false);
		double damage = 0;
		double ignoreShieldsDamage = 0;
		returnDamage = new DamageMeta(recipient);
		returnDamage.isSecondary = true;

		if (owner instanceof PlayerFightData) {
			FightInstance.trigger((Player) owner.getEntity(), Trigger.PRE_DEALT_DAMAGE, new PreDealtDamageEvent(this, target));
		}
		
		// See if any of our effects cancel damage first
		if (recipient instanceof PlayerFightData) {
			PlayerFightData pdata = (PlayerFightData) recipient;
			ReceivedDamageEvent ev = new ReceivedDamageEvent(owner, this);
			if (pdata.runActions(pdata, Trigger.RECEIVED_DAMAGE, ev)) {
				slices.clear();
			}

			if (pdata.hasStatus(StatusType.INVINCIBLE)) {
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
					slices.add(new DamageSlice(ent.getKey(), ent.getValue() * 0.2, DamageType.FIRE));
					if (ent.getKey() instanceof PlayerFightData) {
						((PlayerFightData) ent.getKey()).getStats().addBurnDamage(ent.getValue() * 0.2);
					}
				}
			}

			if (owner.hasStatus(StatusType.SANCTIFIED)) {
				Status status = owner.getStatus(StatusType.SANCTIFIED);
				int stacks = status.getStacks();
				int toRemove = (int) (stacks * 0.25);
				status.apply(owner, -toRemove, 0); // Remove 25% of stacks

				for (Entry<FightData, Integer> ent : recipient.getStatus(StatusType.SANCTIFIED).getSlices().getSliceOwners().entrySet()) {
					if (ent.getKey() instanceof PlayerFightData) {
						((PlayerFightData) ent.getKey()).getStats().addSanctifiedHealing(ent.getValue() * 0.25);
						FightInstance.giveHeal(ent.getKey().getEntity(), ent.getValue() * 0.25, recipient.getEntity()); // Assumes sanctified owner is always a player
					}
				}
			}
			
			if (owner.hasStatus(StatusType.FROST) && containsType(BuffType.MAGICAL)) {
				Status status = owner.getStatus(StatusType.FROST);
				int stacks = status.getStacks();
				int toRemove = (int) (-stacks * 0.25);
				status.apply(owner, toRemove, 0);
				
				for (Entry<FightData, Integer> ent : recipient.getStatus(StatusType.SANCTIFIED).getSlices().getSliceOwners().entrySet()) {
					if (ent.getKey() instanceof PlayerFightData) {
						((PlayerFightData) ent.getKey()).getStats().addFrostMitigated(ent.getValue() * 0.25);
					}
				}
			}

			if (owner.hasStatus(StatusType.CONCUSSED) && containsType(BuffType.PHYSICAL)) {
				Status status = owner.getStatus(StatusType.CONCUSSED);
				int stacks = status.getStacks();
				int toRemove = (int) (-stacks * 0.25);
				status.apply(owner, toRemove, 0);
				
				for (Entry<FightData, Integer> ent : recipient.getStatus(StatusType.CONCUSSED).getSlices().getSliceOwners().entrySet()) {
					if (ent.getKey() instanceof PlayerFightData) {
						((PlayerFightData) ent.getKey()).getStats().addConcussedMitigated(ent.getValue() * 0.25);
					}
				}
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
					if (b.getOrigin() != null && !origins.contains(b.getOrigin())) continue;
					increase += b.getIncrease();
					mult += b.getMultiplier();
					if (!(owner instanceof PlayerFightData)) continue; // Don't need stats for non-player damager
				}
			}

			for (BuffType bt : slice.getPostBuffType().getBuffTypes()) {
				if (!defenseBuffs.containsKey(bt)) continue;
				for (BuffMeta bm : defenseBuffs.get(bt)) {
					Buff b = bm.buff;
					if (b.getOrigin() != null && !origins.contains(b.getOrigin())) continue;
					increase -= b.getIncrease();
					mult -= b.getMultiplier();
					if (!(recipient instanceof PlayerFightData)) continue; // Don't need stats for non-player mitigation
					for (Entry<FightData, BuffSlice> ent : b.getSlices().entrySet()) {
						BuffSlice bs = ent.getValue();
						if (ent.getKey() instanceof PlayerFightData) {
							PlayerFightData buffOwner = (PlayerFightData) ent.getKey();
							double amt = bs.getIncrease() + (bs.getMultiplier() * slice.getDamage());
							if (bm.origin == BuffOrigin.BARRIER) {
								buffOwner.getStats().addDamageBarriered(amt);
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
					pl.getStats().addEvadeMitigated(pl.getStamina());
					pl.setStamina(0);
				}
				else {
					pl.addStamina(-sliceDamage);
					pl.getStats().addEvadeMitigated(sliceDamage);
					sliceDamage = 0;
				}
			}

			// Handle injury
			while (owner.hasStatus(StatusType.INJURY) && sliceDamage > 0) {
				Status injury = owner.getStatus(StatusType.INJURY);
				int stacks = injury.getStacks();
				HashMap<FightData, Integer> owners = recipient.getStatus(StatusType.EVADE).getSlices().getSliceOwners();
				int numOwners = owners.size();
				if (stacks * 0.2 >= sliceDamage) {
					int toRemove = (int) (sliceDamage / 0.2);
					injury.apply(owner, -toRemove, -1);
				
					for (Entry<FightData, Integer> ent : owners.entrySet()) {
						if (ent.getKey() instanceof PlayerFightData) {
							((PlayerFightData) ent.getKey()).getStats().addInjuryMitigated(sliceDamage / numOwners);
						}
					}
					sliceDamage = 0;
				}
				else {
					sliceDamage -= stacks * 0.2;
					injury.apply(owner, -stacks, -1);
				
					for (Entry<FightData, Integer> ent : recipient.getStatus(StatusType.CONCUSSED).getSlices().getSliceOwners().entrySet()) {
						if (ent.getKey() instanceof PlayerFightData) {
							((PlayerFightData) ent.getKey()).getStats().addInjuryMitigated((stacks * 0.2) / numOwners);
						}
					}
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
				int stacks = recipient.getStatus(StatusType.THORNS).getStacks();
				returnDamage.addDamageSlice(new DamageSlice(recipient, stacks, DamageType.THORNS));
				if (recipient instanceof PlayerFightData) {
					((PlayerFightData) recipient).getStats().addThornsDamage(stacks);
				}
			}
			if (recipient.hasStatus(StatusType.REFLECT) && slice.getPostBuffType().containsBuffType(BuffType.MAGICAL)) {
				int stacks = recipient.getStatus(StatusType.REFLECT).getStacks();
				returnDamage.addDamageSlice(new DamageSlice(recipient, stacks, DamageType.REFLECT));
				if (recipient instanceof PlayerFightData) {
					((PlayerFightData) recipient).getStats().addReflectDamage(stacks);
				}
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
		
		if (recipient instanceof PlayerFightData) {
			PlayerFightData pdata = (PlayerFightData) recipient;
			// trigger received health damage
			if (damage > 0 || ignoreShieldsDamage > 0) {
				ReceivedHealthDamageEvent ev = new ReceivedHealthDamageEvent(damager, this, damage, ignoreShieldsDamage);
				if (pdata.runActions(pdata, Trigger.RECEIVED_HEALTH_DAMAGE, ev)) {
					damage = 0;
					ignoreShieldsDamage = 0;
				}
			}
			// all damage was mitigated via buffs or shields
			else {
				Sounds.block.play((Player) recipient.getEntity(), recipient.getEntity());
			}
		}
		double finalDamage = damage + ignoreShieldsDamage + target.getAbsorptionAmount();
		if (damage + ignoreShieldsDamage > 0) {
			
			// Mobs shouldn't have a source of damage because they'll infinitely re-trigger ~OnAttack
			// Players must have a source of damage to get credit for kills, otherwise mobs that suicide give points
			if (owner instanceof PlayerFightData) {

				// Apparently minecraft applies armor based on the entity's disguise if it has one
				EntityType type = DisguiseAPI.isDisguised(target) ? DisguiseAPI.getDisguise(target).getType().getEntityType() : target.getType();
				if (armoredEntities.contains(type)) finalDamage *= 1.09; // To deal with minecraft vanilla armor, rounded up for inconsistency
				FightInstance.trigger((Player) owner.getEntity(), Trigger.DEALT_DAMAGE, new DealtDamageEvent(this, target, damage, ignoreShieldsDamage));
				target.damage(finalDamage, owner.getEntity());
			}
			else {
				target.damage(finalDamage);
			}
			if (!(target instanceof Player)) {
				recipient.updateDisplayName();
				Location loc = target.getLocation().add(0, 1, 0);
				Vector btwn = owner.getEntity().getLocation().subtract(loc).toVector();
				btwn.setY(0);
				btwn.normalize();
				double x = NeoRogue.gen.nextDouble(0.5), y = NeoRogue.gen.nextDouble(0.5), z = NeoRogue.gen.nextDouble(0.5);
				loc = loc.add(btwn).add(x, y, z);
				recipient.getInstance().createIndicator(Component.text((int) (damage + ignoreShieldsDamage), NamedTextColor.RED), loc);
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
	
	// Used for tracking stats mostly
	public static enum BuffOrigin {
		BARRIER,
		STATUS,
		SHIELD,
		PROJECTILE,
		INITIAL_VELOCITY, // Multiplier of initial projectile velocity
		NORMAL;
	}

	// Used for specifying what a buff applies to
	public static enum DamageOrigin {
		NORMAL,
		PROJECTILE,
		TRAP;
	}
}
