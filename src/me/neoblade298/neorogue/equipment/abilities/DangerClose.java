package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class DangerClose extends Equipment {
	private static final String ID = "DangerClose";
	private double damageIncrease;
	
	public DangerClose(boolean isUpgraded) {
		super(ID, "Danger Close", isUpgraded, Rarity.RARE, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.none());
		damageIncrease = isUpgraded ? 0.10 : 0.06;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String buffId = UUID.randomUUID().toString();
		ActionMeta stacks = new ActionMeta();
		ItemStack icon = item.clone();
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
        inst.setAction((pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.EVADE)) return TriggerResult.keep();
			
			// Increase stack counter
			stacks.addCount(1);
			icon.setAmount(stacks.getCount());
			inst.setIcon(icon);
			
			// Add permanent damage buff
			data.addDamageBuff(DamageBuffType.of(DamageCategory.PHYSICAL), 
					Buff.multiplier(data, damageIncrease, StatTracker.damageBuffAlly(buffId, this, true)));
			
			return TriggerResult.keep();
		});
		
		data.addTrigger(id, Trigger.RECEIVE_STATUS, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FEATHER,
				"Passive. Whenever you " + GlossaryTag.EVADE.tag(this) + ", increase your " + 
				GlossaryTag.PHYSICAL.tag(this) + " damage by <yellow>" + (int)(damageIncrease * 100) + "%</yellow>.");
	}
}
