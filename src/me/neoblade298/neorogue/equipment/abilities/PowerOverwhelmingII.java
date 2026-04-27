package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

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
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreCastUsableEvent;

public class PowerOverwhelmingII extends Equipment {
	private static final String ID = "PowerOverwhelmingII";
	private int manaReduc;
	private int shields;
	private int cdReduc;

	public PowerOverwhelmingII(boolean isUpgraded) {
		super(ID, "Power Overwhelming II", isUpgraded, Rarity.EPIC, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, isUpgraded ? 80 : 100, 40, 0));
		manaReduc = isUpgraded ? 30 : 20;
		shields = isUpgraded ? 6 : 4;
		cdReduc = isUpgraded ? 3 : 2;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta activated = new ActionMeta();
		String procId = id + slot;
		String cdProcId = id + "_cd_" + slot;

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

			// Reduce all cooldowns
			ev.addBuff(PropertyType.COOLDOWN, cdProcId,
					Buff.increase(data, -cdReduc, BuffStatTracker.of(cdProcId, this, PropertyType.COOLDOWN.getDisplay() + " reduced")));

			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			if (!activated.getBool()) return TriggerResult.keep();
			CastUsableEvent ev = (CastUsableEvent) in;
			if (ev.getInstance().getEquipment().getType() != EquipmentType.ABILITY) return TriggerResult.keep();

			Player p = data.getPlayer();
			if (p == null) return TriggerResult.keep();

			data.addSimpleShield(p.getUniqueId(), shields, 600); // 600 ticks = 30s
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
						+ ", up to <white>half</white> of each ability's base mana cost, and reduce all cooldowns by " + DescUtil.yellow(cdReduc)
						+ " seconds. Every ability cast grants " + DescUtil.yellow(shields) + " <yellow>shields</yellow> for <white>30s</white>. Can only be cast once.");
	}
}
