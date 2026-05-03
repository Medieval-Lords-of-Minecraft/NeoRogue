package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.Rift;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreCastUsableEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class IAmAtomic extends Equipment {
	private static final String ID = "IAmAtomic";
	private static final ParticleContainer pc = new ParticleContainer(Particle.ENCHANT).count(25).spread(0.5, 0.5).speed(0.1);
	private static final ParticleContainer atomicEffect = new ParticleContainer(Particle.ELECTRIC_SPARK).count(20).spread(0.5, 0.5).speed(0.3);
	private static final double DAMAGE_INCREASE_PER_THRESHOLD = 0.2; // 20% per threshold
	
	private int intel, riftThres, manaReduction, maxManaIncrease, manaThreshold;
	
	public IAmAtomic(boolean isUpgraded) {
		super(ID, "I Am Atomic", isUpgraded, Rarity.EPIC, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		intel = 3;
		riftThres = isUpgraded ? 3 : 4;
		manaReduction = isUpgraded ? 30 : 20;
		maxManaIncrease = isUpgraded ? 5 : 3;
		manaThreshold = isUpgraded ? 40 : 50;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupReforges() {
		addSelfReforge(Entropy.get());
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta killTracker = new ActionMeta();
		ActionMeta lastDamageType = new ActionMeta(); // Stores the last damage type
		ActionMeta manaReductionTracker = new ActionMeta(); // Tracks mana reduction stacks
		ItemStack icon = item.clone();
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		String buffId = UUID.randomUUID().toString();
		String procId = id + slot;
		
		// Original Entropy mechanics - gain intellect on kill, spawn rifts
		data.addTrigger(id, Trigger.KILL, inst);
		inst.setAction((pdata, in) -> {
			Player p = data.getPlayer();
			if (killTracker.getTime() + (properties.get(PropertyType.COOLDOWN) * 1000) > System.currentTimeMillis()) {
				return TriggerResult.keep();
			}
			killTracker.addCount(1);
			data.applyStatus(StatusType.INTELLECT, data, intel, -1);
			Sounds.enchant.play(p, p);
			pc.play(p, p);
			if (killTracker.getCount() % riftThres == 0) {
				Sounds.fire.play(p, p);
				data.addRift(new Rift(data, p.getLocation(), 160));
			}
			icon.setAmount(killTracker.getCount());
			inst.setIcon(icon);
			killTracker.setTime(System.currentTimeMillis());
			return TriggerResult.keep();
		});
		
		// New mechanic - track damage types and apply mana reduction + max mana increase when type changes
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			Player p = data.getPlayer();
			
			// Get the primary damage type from the first slice
			if (ev.getMeta().getSlices().isEmpty()) return TriggerResult.keep();
			DamageSlice primarySlice = ev.getMeta().getSlices().getFirst();
			DamageType currentType = primarySlice.getPostBuffType();
			
			// Check if this is a different type from the last damage dealt
			DamageType lastType = (DamageType) lastDamageType.getObject();
			if (lastType == null || currentType != lastType) {
				// Increase max mana permanently
				data.addMaxMana(maxManaIncrease);
				
				// Add mana reduction stack for next cast
				manaReductionTracker.addCount(1);
				
				// Visual and audio feedback
				atomicEffect.play(p, p);
				Sounds.levelup.play(p, p);
				
				// Update last damage type
				lastDamageType.setObject(currentType);
			}
			
			return TriggerResult.keep();
		});
		
		// Apply mana cost reduction when casting abilities
		data.addTrigger(id, Trigger.PRE_CAST_USABLE, (pdata, in) -> {
			PreCastUsableEvent ev = (PreCastUsableEvent) in;
			if (ev.getInstance().getEquipment().getType() != EquipmentType.ABILITY) return TriggerResult.keep();
			
			int stacks = manaReductionTracker.getCount();
			if (stacks <= 0) return TriggerResult.keep();
			
			double baseManaCost = ev.getInstance().getManaCost();
			if (baseManaCost <= 0) return TriggerResult.keep();
			
			// Apply mana reduction and consume stack
			ev.addBuff(PropertyType.MANA_COST, procId,
					Buff.increase(data, manaReduction,
							BuffStatTracker.of(procId, this, PropertyType.MANA_COST.getDisplay() + " reduced")));
			
			manaReductionTracker.setCount(0); // Consume the reduction for the next cast
			return TriggerResult.keep();
		});
		
		// Apply damage increase based on current mana
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			
			// Calculate damage buff based on mana
			int manaThresholds = (int) (data.getMana() / manaThreshold);
			if (manaThresholds <= 0) return TriggerResult.keep();
			
			double damageMultiplier = manaThresholds * DAMAGE_INCREASE_PER_THRESHOLD;
			
			ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
					Buff.multiplier(data, damageMultiplier, StatTracker.damageBuffAlly(buffId, this)));
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.TOTEM_OF_UNDYING,
				"Passive. Gain " + GlossaryTag.INTELLECT.tag(this, intel, true) + " on kill. Every " + 
				DescUtil.yellow(riftThres) + " kills, spawn a " + GlossaryTag.RIFT.tag(this) + 
				" [<white>8s</white>] at your location. Whenever you deal a damage type that is different " +
				"from your previous damage type, reduce your next ability cast by " + 
				DescUtil.yellow(manaReduction) + " mana and increase your max mana by " + 
				DescUtil.yellow(maxManaIncrease) + ". Damage dealt is increased by " + 
				DescUtil.yellow("20%") + " for every " + DescUtil.yellow(manaThreshold) + 
				" mana you have.");
	}
}
