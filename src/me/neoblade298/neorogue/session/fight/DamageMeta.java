package me.neoblade298.neorogue.session.fight;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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
import me.neoblade298.neorogue.session.fight.buff.BuffList;
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
	private DamageMeta returnDamage;
	private HashMap<DamageBuffType, BuffList> damageBuffs = new HashMap<DamageBuffType, BuffList>(), defenseBuffs = new HashMap<DamageBuffType, BuffList>();
	
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
		this.damageBuffs = cloneBuffLists(original.damageBuffs);
		this.defenseBuffs = cloneBuffLists(original.defenseBuffs);
	}

	private static HashMap<DamageBuffType, BuffList> cloneBuffLists(HashMap<DamageBuffType, BuffList> buffList) {
		HashMap<DamageBuffType, BuffList> clone = new HashMap<DamageBuffType, BuffList>();
		for (Entry<DamageBuffType, BuffList> entry : buffList.entrySet()) {
			clone.put(entry.getKey(), entry.getValue().clone());
		}
		return clone;
	}

	public void setOwner(FightData owner) {
		this.owner = owner;
	}

	public void setProjectileInstance(IProjectileInstance inst) {
		this.proj = inst;
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
	
	public void addDamageBuffLists(HashMap<DamageBuffType, BuffList> buffLists) {
		for (Entry<DamageBuffType, BuffList> entry : buffLists.entrySet()) {
			DamageBuffType type = entry.getKey();
			BuffList list = damageBuffs.getOrDefault(type, new BuffList());
			list.add(entry.getValue());
			damageBuffs.put(type, list);
		}
	}
	
	public void addDefenseBuffLists(HashMap<DamageBuffType, BuffList> buffLists) {
		for (Entry<DamageBuffType, BuffList> entry : buffLists.entrySet()) {
			DamageBuffType type = entry.getKey();
			BuffList list = defenseBuffs.getOrDefault(type, new BuffList());
			list.add(entry.getValue());
			defenseBuffs.put(type, list);
		}
	}

	public void addDamageBuff(DamageBuffType type, Buff b) {
		BuffList list = damageBuffs.getOrDefault(type, new BuffList());
		list.add(b);
		damageBuffs.put(type, list);
	}

	public void addDefenseBuff(DamageBuffType type, Buff b) {
		BuffList list = defenseBuffs.getOrDefault(type, new BuffList());
		list.add(b);
		defenseBuffs.put(type, list);
	}
	
	public double dealDamage(LivingEntity target) {
		if (slices.isEmpty()) return 0;
		if (target.getType() == EntityType.ARMOR_STAND) return 0;
		FightData recipient = FightInstance.getFightData(target.getUniqueId());
		LivingEntity damager = owner.getEntity();
		addDamageBuffLists(owner.getDamageBuffLists());
		addDefenseBuffLists(recipient.getDefenseBuffLists());
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
			addDefenseBuffLists(recipient.getBarrier().getBuffLists());
		}

		// Status effects
		if (!isSecondary) {
			if (recipient.hasStatus(StatusType.BURN)) {
				for (Entry<FightData, Integer> ent : recipient.getStatus(StatusType.BURN).getSlices().getSliceOwners().entrySet()) {
					slices.add(new DamageSlice(ent.getKey(), ent.getValue() * 0.2, DamageType.FIRE));
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
			
			if (owner.hasStatus(StatusType.FROST) && containsType(DamageCategory.MAGICAL)) {
				Status status = owner.getStatus(StatusType.FROST);
				int stacks = status.getStacks();
				int toRemove = (int) (-stacks * 0.25);
				status.apply(owner, toRemove, 0);
			}

			if (owner.hasStatus(StatusType.CONCUSSED) && containsType(DamageCategory.PHYSICAL)) {
				Status status = owner.getStatus(StatusType.CONCUSSED);
				int stacks = status.getStacks();
				int toRemove = (int) (-stacks * 0.25);
				status.apply(owner, toRemove, 0);
			}
		}
		
		// Calculate buffs for every slice of damage
		boolean evading = recipient.hasStatus(StatusType.EVADE) && 
				(slices.isEmpty() ? false : DamageCategory.GENERAL.hasType(slices.getFirst().getPostBuffType()));
		if (evading) {
			recipient.getStatus(StatusType.EVADE).apply(recipient, -1, -1);
		}
		for (DamageSlice slice : slices) {
			double increase = 0, mult = 1, base = slice.getDamage();
			ArrayList<Buff> damBuffs = new ArrayList<Buff>(), defBuffs = new ArrayList<Buff>();
			for (DamageBuffType dbt : getDamageBuffTypes(slice.getType().getCategories())) {
				if (!damageBuffs.containsKey(dbt)) continue;
				BuffList list = damageBuffs.get(dbt);
				increase += list.getIncrease();
				mult += list.getMultiplier();
				damBuffs.addAll(list.getBuffs());
			}

			for (DamageBuffType dbt : getDamageBuffTypes(slice.getPostBuffType().getCategories())) {
				if (!defenseBuffs.containsKey(dbt)) continue;
				BuffList list = damageBuffs.get(dbt);
				increase -= list.getIncrease();
				mult -= list.getMultiplier();
				defBuffs.addAll(list.getBuffs());
			}
			
			// Set the slice damage to at most the target's health so the stats don't overcount damage
			double sliceDamage = Math.max(0, (slice.getDamage() * mult) + increase);
			if (damage + ignoreShieldsDamage + sliceDamage > target.getHealth()) {
				sliceDamage = target.getHealth() - damage - ignoreShieldsDamage;
			}

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

			// Handle statistics if the owner is a player
			if (owner instanceof PlayerFightData) {
				// Sort buffs by greatest positive impact to greatest negative impact
				Comparator<Buff> comp = new Comparator<Buff>() {
					@Override
					public int compare(Buff b1, Buff b2) {
						return Double.compare(b1.getEffectiveChange(base), b2.getEffectiveChange(base));
					}
				};
				damBuffs.sort(comp);
				defBuffs.sort(comp);
				handleBuffStatistics(sliceDamage, damBuffs, defBuffs);
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
			if (recipient.hasStatus(StatusType.THORNS) && DamageCategory.PHYSICAL.hasType(slice.getPostBuffType())) {
				int stacks = recipient.getStatus(StatusType.THORNS).getStacks();
				returnDamage.addDamageSlice(new DamageSlice(recipient, stacks, DamageType.THORNS));
				if (recipient instanceof PlayerFightData) {
					((PlayerFightData) recipient).getStats().addThornsDamage(stacks);
				}
			}
			if (recipient.hasStatus(StatusType.REFLECT) && DamageCategory.MAGICAL.hasType(slice.getPostBuffType())) {
				int stacks = recipient.getStatus(StatusType.REFLECT).getStacks();
				returnDamage.addDamageSlice(new DamageSlice(recipient, stacks, DamageType.REFLECT));
				if (recipient instanceof PlayerFightData) {
					((PlayerFightData) recipient).getStats().addReflectDamage(stacks);
				}
			}
			// Stop counting damage slices after the target is already dead
			if (damage + ignoreShieldsDamage >= target.getHealth()) break;
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

	private Collection<DamageBuffType> getDamageBuffTypes(Collection<DamageCategory> cats) {
		Collection<DamageBuffType> types = new ArrayList<DamageBuffType>();
		for (DamageCategory cat : cats) {
			for (DamageOrigin origin : origins) {
				types.add(DamageBuffType.of(cat, origin));
			}
		}
		return types;
	}

	private void handleBuffStatistics(double sliceDamage, ArrayList<Buff> damageBuffs, ArrayList<Buff> defenseBuffs) {
		if (sliceDamage > 0) {
			// Since damage was dealt, all defense buffs are calculated in the stat
			for (Buff b : defenseBuffs) {

			}
		}
		else {

		}
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

	public boolean containsType(DamageCategory cat) {
		for (DamageSlice slice : slices) {
			if (cat.hasType(slice.getPostBuffType())) return true;
		}
		return false;
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
		FROST,
		SHIELD,
		STATUS,
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
