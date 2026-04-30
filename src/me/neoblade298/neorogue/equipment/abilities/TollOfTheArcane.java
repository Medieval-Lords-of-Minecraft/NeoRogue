package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
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

public class TollOfTheArcane extends Equipment {
	private static final String ID = "TollOfTheArcane";
	private int corruption;
	private double manaReduc;
	private int manaReducDisplay;

	public TollOfTheArcane(boolean isUpgraded) {
		super(ID, "Toll of the Arcane", isUpgraded, Rarity.EPIC, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, isUpgraded ? 80 : 100, 40, 0));
		corruption = 3;
		manaReduc = isUpgraded ? 0.7 : 0.5;
		manaReducDisplay = (int) (manaReduc * 100);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta activated = new ActionMeta();
		String procId = id + slot;

		data.addTrigger(id, Trigger.PRE_CAST_USABLE, (pdata, in) -> {
			if (!activated.getBool()) return TriggerResult.keep();
			PreCastUsableEvent ev = (PreCastUsableEvent) in;
			if (ev.getInstance().getEquipment().getType() != EquipmentType.ABILITY) return TriggerResult.keep();
			if (ev.getInstance().getManaCost() <= 0) return TriggerResult.keep();

			ev.addBuff(PropertyType.MANA_COST, procId,
					Buff.multiplier(data, manaReduc,
							BuffStatTracker.of(procId, this, PropertyType.MANA_COST.getDisplay() + " reduced")));
			return TriggerResult.keep();
		});

		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			activated.setBool(true);
			data.applyStatus(StatusType.CORRUPTION, data, corruption, -1);
			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CRYING_OBSIDIAN,
				"On cast, decrease mana costs of all castable abilities by " + DescUtil.yellow(manaReducDisplay + "%")
						+ " and apply " + GlossaryTag.CORRUPTION.tag(this, corruption, false)
						+ " to yourself. Can only be cast once.");
	}
}