package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;

import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreCastUsableEvent;

public class HeartOfTheStorm extends Equipment {
	private static final String ID = "HeartOfTheStorm";
	private static final double MANA_REDUCTION = 0.5;
	private int threshold;

	public HeartOfTheStorm(boolean isUpgraded) {
		super(ID, "Heart of the Storm", isUpgraded, Rarity.EPIC, EquipmentClass.MAGE,
				EquipmentType.ACCESSORY, EquipmentProperties.none());
		threshold = isUpgraded ? 80 : 100;
	}

	public static Equipment get() { return Equipment.get(ID, false); }

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta progress = new ActionMeta();
		String buffId = id + slot;
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent event = (ApplyStatusEvent) in;
			if (!event.isStatus(StatusType.ELECTRIFIED) || progress.getBool()) return TriggerResult.keep();
			progress.addCount(Math.max(0, event.getStacks()));
			if (progress.getCount() >= threshold) {
				progress.setBool(true);
				Sounds.thunder.play(data.getPlayer(), data.getPlayer());
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.PRE_CAST_USABLE, (pdata, in) -> {
			if (!progress.getBool()) return TriggerResult.keep();
			PreCastUsableEvent event = (PreCastUsableEvent) in;
			if (event.getInstance().getEquipment().getType() == EquipmentType.ABILITY
					&& event.getInstance().getManaCost() > 0) {
				event.addBuff(PropertyType.MANA_COST, buffId, Buff.multiplier(data, MANA_REDUCTION,
						BuffStatTracker.of(buffId, this, "Mana cost reduced")));
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.HEART_OF_THE_SEA, GlossaryTag.PASSIVE.tag(this) + ". After applying "
				+ GlossaryTag.ELECTRIFIED.tag(this, threshold) + " cumulatively, halve ability mana costs for the rest of the fight.");
	}
}