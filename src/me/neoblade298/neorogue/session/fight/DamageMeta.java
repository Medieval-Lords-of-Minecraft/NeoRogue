package me.neoblade298.neorogue.session.fight;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.bukkit.BukkitAdapter;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.IProjectileInstance;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffList;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealtDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedDamageBarrierEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedHealthDamageEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class DamageMeta {
	private static final DecimalFormat df = new DecimalFormat("##.#");
	
	private FightData owner;
	private boolean hitBarrier, isSecondary;
	private HashSet<DamageOrigin> origins = new HashSet<DamageOrigin>();
	private IProjectileInstance proj; // If the damage originated from projectile
	private LinkedList<DamageSlice> slices = new  LinkedList<DamageSlice>();
	private DamageMeta returnDamage;
	private HashMap<DamageBuffType, BuffList> damageBuffs = new HashMap<DamageBuffType, BuffList>(), defenseBuffs = new HashMap<DamageBuffType, BuffList>();
	private double ignoreShieldsDamage, damage;
	
	public DamageMeta(FightData data) {
		this.owner = data;
		this.origins.add(DamageOrigin.NORMAL);
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
		return dealDamage(target, null);
	}
	
	public double dealDamage(LivingEntity target, @Nullable SkillMetadata skillData) {
		if (slices.isEmpty()) return 0;
		if (target.getType() == EntityType.ARMOR_STAND) return 0;
		FightData recipient = FightInstance.getFightData(target.getUniqueId());
		LivingEntity damager = owner.getEntity();
		if (damager == null) return 0;
		addDamageBuffLists(owner.getDamageBuffLists());
		addDefenseBuffLists(recipient.getDefenseBuffLists());
		returnDamage = new DamageMeta(recipient);
		returnDamage.isSecondary = true;

		// Remove all armor from entity, apparently this can't be done on-spawn because armor is added asynchronously or something
		AttributeInstance armor = target.getAttribute(Attribute.GENERIC_ARMOR);
		AttributeInstance toughness = target.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS);
		armor.setBaseValue(0);
		armor.getModifiers().forEach(mod -> armor.removeModifier(mod));
		toughness.setBaseValue(0);
		toughness.getModifiers().forEach(mod -> armor.removeModifier(mod));

		if (owner instanceof PlayerFightData) {
			FightInstance.trigger((Player) owner.getEntity(), Trigger.PRE_DEALT_DAMAGE, new PreDealtDamageEvent(this, target));
		}
		
		// Reduce damage from barriers, used only for players blocking projectiles
		// For mobs blocking projectiles, go to damageProjectile
		boolean nullifiedByBarrier = false;
		if (hitBarrier && !recipient.getBarriers().isEmpty()) {
			// Figure out which barrier it is
			PlayerFightData pdata = (PlayerFightData) recipient;
			Location loc = BukkitAdapter.adapt(skillData.getOrigin());
			Barrier barrier = null;
			for (Barrier b : recipient.getBarriers().values()) {
				if (b.collides(loc)) {
					barrier = b;
					break;
				}
			}

			if (barrier != null) {
				ReceivedDamageBarrierEvent ev = new ReceivedDamageBarrierEvent(owner, this, barrier);
				if (pdata.runActions(pdata, Trigger.RECEIVED_DAMAGE_BARRIER, ev) || barrier.isUnbreakable()) {
					nullifiedByBarrier = true;
				}
				else {
					addDefenseBuffLists(barrier.getBuffLists());
				}
			}
		}

		// See if any of our effects cancel damage after barrier
		boolean cancelDamage = false;
		if (recipient instanceof PlayerFightData && !nullifiedByBarrier) {
			PlayerFightData pdata = (PlayerFightData) recipient;
			ReceivedDamageEvent ev = new ReceivedDamageEvent(owner, this);
			if (pdata.runActions(pdata, Trigger.RECEIVED_DAMAGE, ev) || pdata.hasStatus(StatusType.INVINCIBLE)) {
				cancelDamage = true;
			}
		}

		// Status effects
		if (!isSecondary) {
			if (recipient.hasStatus(StatusType.SANCTIFIED) && containsType(DamageCategory.LIGHT)) {
				for (Entry<FightData, Integer> ent : owner.getStatus(StatusType.SANCTIFIED).getSlices().getSliceOwners().entrySet()) {
					if (ent.getKey() instanceof PlayerFightData) {
						((PlayerFightData) ent.getKey()).getStats().addSanctifiedShielding(ent.getValue() * 0.01);
						recipient.addSimpleShield(ent.getKey().getUniqueId(), ent.getValue() * 0.01, 60);
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
		
		boolean isStatusDamage = !DamageCategory.GENERAL.hasType(slices.getFirst().getPostBuffType());
		// Calculate buffs for every slice of damage
		HashMap<DamageType, Double> statSlices = new HashMap<DamageType, Double>();
		for (DamageSlice slice : slices) {
			double increase = 0, mult = 1, base = slice.getDamage();
			ArrayList<Buff> buffs = new ArrayList<Buff>(), debuffs = new ArrayList<Buff>();
			if (slice.getType() == null) {
				Bukkit.getLogger().warning("[NeoRogue] Damage slice has null type dealing " + slice.getDamage() + " damage");
				continue;
			}

			Collection<DamageBuffType> categories = getDamageBuffTypes(slice.getType().getCategories());
			for (DamageBuffType dbt : categories) {
				if (!damageBuffs.containsKey(dbt)) continue;
				BuffList list = damageBuffs.get(dbt);
				increase += list.getIncrease();
				mult += list.getMultiplier();

				for (Buff b : list.getBuffs()) {
					if (b.isPositive(base)) buffs.add(b);
					else debuffs.add(b);
				}
			}

			for (DamageBuffType dbt : categories) {
				if (!defenseBuffs.containsKey(dbt)) continue;
				BuffList list = defenseBuffs.get(dbt);
				increase -= list.getIncrease();
				mult -= list.getMultiplier();

				for (Buff b : list.getBuffs()) {
					if (!b.isPositive(base)) buffs.add(b);
					else debuffs.add(b);
				}
			}
			
			// Set the slice damage to at most the target's health so the stats don't overcount damage
			double sliceDamage = Math.max(0, (slice.getDamage() * mult) + increase);
			if (damage + ignoreShieldsDamage + sliceDamage > target.getHealth()) {
				sliceDamage = target.getHealth() - damage - ignoreShieldsDamage;
			}

			// Sort buffs by greatest positive impact to greatest negative impact
			final double sliceDamageFinal = sliceDamage;
			Comparator<Buff> comp = new Comparator<Buff>() {
				@Override
				public int compare(Buff b1, Buff b2) {
					double change1 = b1.getEffectiveChange(sliceDamageFinal);
					double change2 = b2.getEffectiveChange(sliceDamageFinal);
					return Double.compare(change2, change1);
				}
			};

			/* 
			 * Split buffs into positive (increases damage) and negative (decreases damage)
			 * If damage > 0, all negative buffs get full stats
			 * Subtract base damage from final damage, then positive buffs get stats in priority of
			 * highest effective change to lowest
			 * If damage <= 0, skip positive buff stats entirely
			 * Apply all positive buffs to the base damage, then give debuffs stats based on
			 * highest effective change to lowest
			 */
			 if (sliceDamage > 0) {
				for (Buff b : debuffs) {
					if (b.getStatTracker() == null) continue;
					if (!(b.getApplier() instanceof PlayerFightData)) continue;
					((PlayerFightData) b.getApplier()).getStats().addBuffStat(b.getStatTracker(), b.getEffectiveChange(base));
				}

				double temp = sliceDamage - base;
				buffs.sort(comp);
				for (Buff b : buffs) {
					if (temp <= 0) break;
					if (b.getStatTracker() == null) continue;
					if (!(b.getApplier() instanceof PlayerFightData)) continue;
					double change = b.getEffectiveChange(base);
					((PlayerFightData) b.getApplier()).getStats().addBuffStat(b.getStatTracker(), Math.min(temp, change));
					temp -= change;
				}
			}
			else {
				double temp = base, inc = 0, multi = 1;
				for (Buff b : buffs) {
					inc += b.getIncrease();
					multi += b.getMultiplier();
				}

				temp = (temp * multi) + inc;
				for (Buff b : debuffs) {
					if (temp <= 0) break;
					if (b.getStatTracker() == null) continue;
					if (!(b.getApplier() instanceof PlayerFightData)) continue;
					double change = b.getEffectiveChange(temp);
					((PlayerFightData) b.getApplier()).getStats().addBuffStat(b.getStatTracker(), Math.min(temp, change));
					temp -= change;
				}
			}
			// Save the damage values per damage type to put into stats later
			double temp = statSlices.getOrDefault(slice.getPostBuffType(), 0.0) + sliceDamageFinal;
			statSlices.put(slice.getPostBuffType(), temp);

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
			}
			if (recipient.hasStatus(StatusType.REFLECT) && DamageCategory.MAGICAL.hasType(slice.getPostBuffType())) {
				int stacks = recipient.getStatus(StatusType.REFLECT).getStacks();
				returnDamage.addDamageSlice(new DamageSlice(recipient, stacks, DamageType.REFLECT));
			}
			// Stop counting damage slices after the target is already dead
			if (damage + ignoreShieldsDamage >= target.getHealth()) break;
		}
		
		// Barrier nullification
		if (nullifiedByBarrier) {
			PlayerFightData pl = (PlayerFightData) recipient; // Only players can have barrier
			pl.getStats().addDamageBarriered(damage + ignoreShieldsDamage);
			damage = 0;
			ignoreShieldsDamage = 0;
			statSlices.clear();
		}
		
		// General damage nullification
		if (cancelDamage && recipient instanceof PlayerFightData) {
			PlayerFightData pl = (PlayerFightData) recipient;
			pl.getStats().addDamageNullified(damage + ignoreShieldsDamage);
			damage = 0;
			ignoreShieldsDamage = 0;
			statSlices.clear();
		}

		// Evade
		if (recipient.hasStatus(StatusType.EVADE) && !isStatusDamage && (damage > 0 || ignoreShieldsDamage > 0)) {
			if (recipient.getEntity().getType() == EntityType.PLAYER) Sounds.attackSweep.play((Player) recipient.getEntity(), recipient.getEntity());
			recipient.applyStatus(StatusType.EVADE, recipient, -1, -1);
			double totalDamage = damage + ignoreShieldsDamage;
			PlayerFightData pl = (PlayerFightData) recipient; // Only players can have evade status
			if (totalDamage < pl.getStamina()) {
				damage = 0;
				ignoreShieldsDamage = 0;
				statSlices.clear();
				pl.getStats().addEvadeMitigated(pl.getStamina());
				pl.setStamina(0);
			}
			else {
				if (ignoreShieldsDamage < pl.getStamina()) {
					pl.addStamina(ignoreShieldsDamage);
					subtractFromStats(statSlices, ignoreShieldsDamage);
					ignoreShieldsDamage = 0;
				}
				else {
					ignoreShieldsDamage -= pl.getStamina();
					subtractFromStats(statSlices, pl.getStamina());
					pl.setStamina(0);
				}
				
				damage -= pl.getStamina();
				subtractFromStats(statSlices, pl.getStamina());
				pl.setStamina(0);
			}
		}

		// Injury
		if (owner.hasStatus(StatusType.INJURY) && !isStatusDamage && (damage > 0 || ignoreShieldsDamage > 0)) {
			double totalDamage = damage + ignoreShieldsDamage;
			Status injury = owner.getStatus(StatusType.INJURY);
			int stacks = injury.getStacks();
			HashMap<FightData, Integer> owners = owner.getStatus(StatusType.INJURY).getSlices().getSliceOwners();
			int numOwners = owners.size();
			// Full block with injury
			if (stacks * 0.2 >= totalDamage) {
				int toRemove = (int) (totalDamage / 0.2);
				injury.apply(owner, -toRemove, -1);
				for (Entry<FightData, Integer> ent : owners.entrySet()) {
					if (ent.getKey() instanceof PlayerFightData) {
						((PlayerFightData) ent.getKey()).getStats().addInjuryMitigated(totalDamage / numOwners);
					}
				}
				statSlices.clear();
				damage = 0;
				ignoreShieldsDamage = 0;
			}
			// No full block with injury
			else {
				injury.apply(owner, -stacks, -1);
				for (Entry<FightData, Integer> ent : owners.entrySet()) {
					if (ent.getKey() instanceof PlayerFightData) {
						((PlayerFightData) ent.getKey()).getStats().addInjuryMitigated(stacks * 0.2 / numOwners);
					}
				}
				
				// Block ignore shields damage first
				if (stacks * 0.2 >= ignoreShieldsDamage) {
					stacks -= (int) (ignoreShieldsDamage / 0.2);
					subtractFromStats(statSlices, (int) (ignoreShieldsDamage / 0.2));
					ignoreShieldsDamage = 0;
				}
				else {
					ignoreShieldsDamage -= stacks * 0.2;
					subtractFromStats(statSlices, stacks * 0.2);
					stacks = 0;
				}
				subtractFromStats(statSlices, stacks * 0.2);
				damage -= stacks * 0.2;
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
			
		if (owner instanceof PlayerFightData) {
			PlayerFightData pdata = (PlayerFightData) owner;
			for (Entry<DamageType, Double> entry : statSlices.entrySet()) {
				pdata.getStats().addDamageDealt(entry.getKey(), entry.getValue());
			}
		}
		
		if (recipient instanceof PlayerFightData) {
			PlayerFightData pdata = (PlayerFightData) recipient;
			// Apply damage received stats
			String mob = null; // Can be null, just won't show mob display name
			if (NeoRogue.mythicApi.isMythicMob(owner.getEntity())) {
				mob = NeoRogue.mythicApi.getMythicMobInstance(owner.getEntity()).getType().getInternalName();
			}
			for (Entry<DamageType, Double> entry : statSlices.entrySet()) {
				pdata.getStats().addDamageTaken(mob, entry.getKey(), entry.getValue());
			}

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
		double finalDamage = damage + ignoreShieldsDamage + target.getAbsorptionAmount(); // +1 to deal with rounding errors
		if (damage + ignoreShieldsDamage > 0) {
			
			// Mobs shouldn't have a source of damage because they'll infinitely re-trigger ~OnAttack
			// Players must have a source of damage to get credit for kills, otherwise mobs that suicide give points
			if (owner instanceof PlayerFightData) {
				// Apparently minecraft applies armor based on the entity's disguise if it has one
				//EntityType type = DisguiseAPI.isDisguised(target) ? DisguiseAPI.getDisguise(target).getType().getEntityType() : target.getType();
				//if (armoredEntities.contains(type)) finalDamage *= 1.09; // To deal with minecraft vanilla armor, rounded up for inconsistency
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
				recipient.getInstance().createIndicator(Component.text(df.format(damage + ignoreShieldsDamage), NamedTextColor.RED), loc);
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
			target.playHurtAnimation(0);
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
	
	public DamageMeta getReturnDamage() {
		return returnDamage;
	}
	
	public DamageSlice getPrimarySlice() {
		return slices.getFirst();
	}
	
	public LinkedList<DamageSlice> getSlices() {
		return slices;
	}

	public double getDamage() {
		return damage;
	}

	public double getIgnoreShieldsDamage() {
		return ignoreShieldsDamage;
	}

	public double getTotalDamage() {
		return damage + ignoreShieldsDamage;
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

	// Used to subtract a straight number equally from all damage types in the stat slices
	private void subtractFromStats(HashMap<DamageType, Double> slices, double amount) {
		double fromEach = Math.round(amount * 10 / slices.size()) / 10;
		for (Entry<DamageType, Double> entry : slices.entrySet()) {
			double newVal = entry.getValue() - fromEach;
			if (newVal < 0) newVal = 0;
			slices.put(entry.getKey(), newVal);
		}
	}

	@Override
	public String toString() {
		String str = "";
		for (DamageSlice slice : slices) {
			str += slice.toString() + ", ";
		}
		return str;
	}

	// Used for specifying what a buff applies to
	public static enum DamageOrigin {
		NORMAL,
		PROJECTILE,
		TRAP;
	}
}
