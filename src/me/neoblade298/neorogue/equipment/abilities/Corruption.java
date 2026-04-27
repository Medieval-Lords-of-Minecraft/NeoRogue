package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreCastUsableEvent;

public class Corruption extends Equipment {
	private static final String ID = "Corruption";
	private int reduc;

	public Corruption(boolean isUpgraded) {
		super(ID, "Corruption", isUpgraded, Rarity.RARE, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.none());
		reduc = isUpgraded ? 5 : 3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String procId = id + slot;
		data.addTrigger(id, Trigger.PRE_CAST_USABLE, (pdata, in) -> {
			PreCastUsableEvent ev = (PreCastUsableEvent) in;
			if (ev.getInstance().getEquipment().getType() != EquipmentType.ABILITY) return TriggerResult.keep();

			int stacks = data.getStatus(StatusType.CORRUPTION).getStacks();
			if (stacks <= 0) return TriggerResult.keep();

			ev.addBuff(PropertyType.MANA_COST, procId,
					Buff.increase(data, reduc * stacks,
							BuffStatTracker.of(procId, this, PropertyType.MANA_COST.getDisplay() + " reduced")));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FERMENTED_SPIDER_EYE,
				"Passive. Reduce mana cost of all castable abilities by " + DescUtil.yellow(reduc)
						+ " for every stack of " + GlossaryTag.CORRUPTION.tag(this) + " you have.");
	}
}