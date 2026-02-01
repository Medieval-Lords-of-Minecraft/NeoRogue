package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class Quicken extends Equipment {
	private static final String ID = "Quicken";
	private static final int MAX_STACKS = 1;
	
	public Quicken(boolean isUpgraded) {
		super(ID, "Quicken", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, isUpgraded ? 15 : 25, 3, 0));
				properties.addUpgrades(PropertyType.STAMINA_COST);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta stacks = new ActionMeta();
		ActionMeta lastBasicAttackTime = new ActionMeta();
		ItemStack icon = item.clone();
		ItemStack activeIcon = icon.withType(Material.FEATHER);
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		
		// Set condition that stacks must be greater than 0 to cast
		inst.setCondition((pl, pdata, in) -> stacks.getCount() > 0);
		
		// Set action for when the ability is cast
		inst.setAction((pdata, in) -> {
			// Dash forward
			data.dash();
			
			// Grant 1 evade for 5 seconds
			data.applyStatus(StatusType.EVADE, data, 1, 100);
			
			// Reduce stacks by 1 and update icon
			if (stacks.getCount() > 0) {
				stacks.addCount(-1);
			}
			
			if (stacks.getCount() > 0) {
				ItemStack currentIcon = activeIcon.clone();
				currentIcon.setAmount(stacks.getCount());
				inst.setIcon(currentIcon);
			} else {
				inst.setIcon(icon);
			}
			
			return TriggerResult.keep();
		});
		
		// Register the ability on the bind trigger
		data.addTrigger(id, bind, inst);
		
		// Track when basic attacks happen
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			lastBasicAttackTime.setTime(System.currentTimeMillis());
			return TriggerResult.keep();
		});
		
		// On dealing physical damage, check if within 1 second of basic attack
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			
			// Only trigger on physical damage
			if (!ev.getMeta().containsType(DamageCategory.PHYSICAL)) return TriggerResult.keep();
			
			// Check if within 1 second (1000ms) of last basic attack
			long timeSinceBasicAttack = System.currentTimeMillis() - lastBasicAttackTime.getTime();
			if (timeSinceBasicAttack <= 1000 && stacks.getCount() < MAX_STACKS) {
				stacks.addCount(1);
				// Update to active icon when we have a stack
				ItemStack currentIcon = activeIcon.clone();
				currentIcon.setAmount(1);
				inst.setIcon(currentIcon);
			}
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GRAY_DYE,
				"Dealing " + GlossaryTag.PHYSICAL.tag(this) + " damage within <white>1</white> second of a basic attack " +
				"grants a stack. On cast, " + GlossaryTag.DASH.tag(this) + " forward and gain " + 
				GlossaryTag.EVADE.tag(this, 1, false) + " for <white>5s</white>.");
	}
}
