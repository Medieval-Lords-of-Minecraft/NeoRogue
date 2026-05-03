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
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class Brilliance extends Equipment {
	private static final String ID = "Brilliance";
	private static final ParticleContainer pc = new ParticleContainer(Particle.ENCHANT).count(25).spread(0.5, 0.5).speed(0.1);
	private static final ParticleContainer brillianceEffect = new ParticleContainer(Particle.END_ROD).count(15).spread(0.5, 0.5).speed(0.2);
	private static final double RESISTANCE_AMOUNT = 0.5; // 50% resistance
	private static final int RESISTANCE_DURATION = 100; // 5 seconds in ticks
	
	private int intel, riftThres, protectShell;
	
	public Brilliance(boolean isUpgraded) {
		super(ID, "Brilliance", isUpgraded, Rarity.EPIC, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		intel = 3;
		riftThres = isUpgraded ? 3 : 4;
		protectShell = isUpgraded ? 3 : 2;
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
		ItemStack icon = item.clone();
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		String buffId = UUID.randomUUID().toString();
		
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
		
		// New mechanic - track damage types and apply protect, shell, and resistance when type changes
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
				// Apply protect and shell
				data.applyStatus(StatusType.PROTECT, data, protectShell, RESISTANCE_DURATION);
				data.applyStatus(StatusType.SHELL, data, protectShell, RESISTANCE_DURATION);
				
				// Apply 50% resistance to the damage type used
				DamageCategory category = getDamageCategoryFromType(currentType);
				if (category != null) {
					data.addDefenseBuff(DamageBuffType.of(category), 
						Buff.multiplier(data, RESISTANCE_AMOUNT, StatTracker.defenseBuffAlly(buffId, this)), 
						RESISTANCE_DURATION);
				}
				
				// Visual and audio feedback
				brillianceEffect.play(p, p);
				Sounds.levelup.play(p, p);
				
				// Update last damage type
				lastDamageType.setObject(currentType);
			}
			
			return TriggerResult.keep();
		});
	}
	
	// Helper method to get the primary damage category for resistance
	private DamageCategory getDamageCategoryFromType(DamageType type) {
		switch (type) {
			case FIRE: return DamageCategory.FIRE;
			case ICE: return DamageCategory.ICE;
			case LIGHTNING: return DamageCategory.LIGHTNING;
			case EARTHEN: return DamageCategory.EARTHEN;
			case DARK: return DamageCategory.DARK;
			case LIGHT: return DamageCategory.LIGHT;
			case POISON: return DamageCategory.POISON;
			case SLASHING: return DamageCategory.SLASHING;
			case PIERCING: return DamageCategory.PIERCING;
			case BLUNT: return DamageCategory.BLUNT;
			default: return null; // For types without specific categories
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHER_STAR,
				"Passive. Gain " + GlossaryTag.INTELLECT.tag(this, intel, true) + " on kill. Every " + 
				DescUtil.yellow(riftThres) + " kills, spawn a " + GlossaryTag.RIFT.tag(this) + 
				" [<white>8s</white>] at your location. Whenever you deal a damage type that is different " +
				"from your previous damage type, gain " + GlossaryTag.PROTECT.tag(this, protectShell, true) + 
				" and " + GlossaryTag.SHELL.tag(this, protectShell, true) + " [<white>5s</white>] and " +
				"<white>50%</white> resistance to the damage type you used [<white>5s</white>].");
	}
}
