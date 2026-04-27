package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreCastUsableEvent;

public class PowerOverwhelming extends Equipment {
	private static final String ID = "PowerOverwhelming";
	private int manaReduc;

	public PowerOverwhelming(boolean isUpgraded) {
		super(ID, "Power Overwhelming", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, isUpgraded ? 80 : 100, 40, 0));
		manaReduc = isUpgraded ? 30 : 20;
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

			double baseManaCost = ev.getInstance().getManaCost();
			if (baseManaCost <= 0) return TriggerResult.keep();

			double maxReduc = baseManaCost / 2.0;
			double reduc = Math.min(manaReduc, maxReduc);
			ev.addBuff(PropertyType.MANA_COST, procId,
					Buff.increase(data, reduc, BuffStatTracker.of(procId, this, PropertyType.MANA_COST.getDisplay() + " reduced")));
			return TriggerResult.keep();
		});

		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			activated.setBool(true);
			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHER_STAR,
				"On cast, decrease mana costs of all castable abilities by " + DescUtil.yellow(manaReduc)
						+ ", up to <white>half</white> of each ability's base mana cost. Can only be cast once.");
	}
}