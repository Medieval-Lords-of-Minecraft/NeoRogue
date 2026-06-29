package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreCastUsableEvent;

public class PowerOverwhelmingII extends Equipment implements Power {
	private static final String ID = "PowerOverwhelmingII";
	private int manaReduc;
	private int shields;
	private int shieldDuration;
	private int cdReduc;

	public PowerOverwhelmingII(boolean isUpgraded) {
		super(ID, "Power Overwhelming II", isUpgraded, Rarity.EPIC, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.none());
		manaReduc = isUpgraded ? 30 : 20;
		shields = isUpgraded ? 6 : 4;
		shieldDuration = 5;
		cdReduc = isUpgraded ? 3 : 2;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	private static final int ACTIVATION_THRES = 2;

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta activated = new ActionMeta();
		String procId = id + slot;
		String cdProcId = id + "_cd_" + slot;

		// Pre-register the cost/cd reduction trigger (only active after activation)
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

		// Pre-register the shield-on-cast trigger (only active after activation)
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			if (!activated.getBool()) return TriggerResult.keep();
			CastUsableEvent ev = (CastUsableEvent) in;
			if (ev.getInstance().getEquipment().getType() != EquipmentType.ABILITY) return TriggerResult.keep();

			Player p = data.getPlayer();
			if (p == null) return TriggerResult.keep();

			data.addSimpleShield(p.getUniqueId(), shields, shieldDuration * 20);
			return TriggerResult.keep();
		});

		// Activation condition: cast 2 abilities while above 50% mana
		ActionMeta am = new ActionMeta();
		data.addTrigger(id + "_activation", Trigger.CAST_USABLE, (pdata, in) -> {
			if (data.getMana() < data.getMaxMana() * 0.5) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() < ACTIVATION_THRES) return TriggerResult.keep();

			activated.setBool(true);
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {

	}


	@Override
	public void setupItem() {
		item = createItem(Material.NETHER_STAR,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after casting " + DescUtil.white(2) + " abilities while above " + DescUtil.white("50%") + " mana. Decrease mana costs of all castable abilities by " + DescUtil.yellow(manaReduc)
						+ ", up to " + DescUtil.white("half") + " of each ability's base mana cost, and reduce all cooldowns by " + DescUtil.yellow(cdReduc)
						+ " seconds. Every ability cast grants " + GlossaryTag.SHIELDS.tag(this, shields, true) + " [" + DescUtil.white(shieldDuration + "s") + "].");
	}
}
