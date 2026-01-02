package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class AfterImage extends Equipment {
	private static final String ID = "AfterImage";
	private int shieldsPerStack;
	
	public AfterImage(boolean isUpgraded) {
		super(ID, "After Image", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.none());
		shieldsPerStack = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.DASH, new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			// Check if player has stealth stacks
			if (!data.hasStatus(StatusType.STEALTH)) {
				return TriggerResult.keep();
			}
			
			int stealthStacks = data.getStatus(StatusType.STEALTH).getStacks();
			// Cap at 5 stacks
			int effectiveStacks = Math.min(stealthStacks, 5);
			
			if (effectiveStacks > 0) {
				int shieldsToGain = shieldsPerStack * effectiveStacks;
				data.addSimpleShield(p.getUniqueId(), shieldsToGain, 100);
			}
			
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PHANTOM_MEMBRANE,
				"Passive. Every time you " + GlossaryTag.DASH.tag(this) + ", gain " + 
				GlossaryTag.SHIELDS.tag(this, shieldsPerStack, true) + " for every stack of " + GlossaryTag.STEALTH.tag(this) + 
				" you have (up to <white>5</white> stacks) for <white>5s</white>.");
	}
}