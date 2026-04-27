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
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreCastUsableEvent;

public class VoidForm extends Equipment {
	private static final String ID = "VoidForm";
	private int manaReduc, cooldownReduc;

	public VoidForm(boolean isUpgraded) {
		super(ID, "Void Form", isUpgraded, Rarity.EPIC, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.none());
		manaReduc = isUpgraded ? 25 : 15;
		cooldownReduc = isUpgraded ? 4 : 2;
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

			int riftCount = data.getRifts().size();
			if (riftCount <= 0) return TriggerResult.keep();

			ev.addBuff(PropertyType.MANA_COST, procId,
					Buff.increase(data, manaReduc * riftCount,
							BuffStatTracker.of(procId, this, PropertyType.MANA_COST.getDisplay() + " reduced")));
			ev.addBuff(PropertyType.COOLDOWN, procId,
					new Buff(data, cooldownReduc * riftCount, 0,
							BuffStatTracker.of(procId, this, PropertyType.COOLDOWN.getDisplay() + " reduced")));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ENDER_EYE,
				"Passive. For each active " + GlossaryTag.RIFT.tag(this) + ", reduce castable ability mana cost by "
						+ DescUtil.yellow(manaReduc) + " and cooldown by " + DescUtil.yellow(cooldownReduc) + ".");
	}
}