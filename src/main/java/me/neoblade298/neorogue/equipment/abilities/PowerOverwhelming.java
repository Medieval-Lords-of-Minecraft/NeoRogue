package me.neoblade298.neorogue.equipment.abilities;
import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreCastUsableEvent;

public class PowerOverwhelming extends Equipment implements Power {
	private static final String ID = "PowerOverwhelming";
	private int manaReduc;

	public PowerOverwhelming(boolean isUpgraded) {
		super(ID, "Power Overwhelming", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.none());
		manaReduc = isUpgraded ? 25 : 15;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupReforges() {
		addReforge(CatalystCrucible.get(), TollOfTheArcane.get());
		addReforge(ArchmagesTome.get(), PowerOverwhelmingII.get());
	}

	private static final int ACTIVATION_THRES = 2;

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		// Activation condition: cast 2 abilities while above 50% mana
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			if (data.getMana() < data.getMaxMana() * 0.5) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() < ACTIVATION_THRES) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		String procId = id + slot;
		data.addTrigger(id, Trigger.PRE_CAST_USABLE, (pdata, in) -> {
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

	}


	@Override
	public void setupItem() {
		item = createItem(Material.NETHER_STAR,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after casting " + DescUtil.val(2) + " abilities while above " + DescUtil.val("50%") + " mana. Decrease mana costs of all castable abilities by " + DescUtil.val(manaReduc)
						+ ", up to " + DescUtil.val("half") + " of each ability's base mana cost, for the duration of the fight.");
	}
}