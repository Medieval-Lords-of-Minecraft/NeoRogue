package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class MajorStrengthRelic extends Equipment {
	private static final String ID = "MajorStrengthRelic";
	private static final int BONUS_STRENGTH = 1;
	private int threshold;

	public MajorStrengthRelic(boolean isUpgraded) {
		super(ID, "Major Strength Relic", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ACCESSORY);
		threshold = isUpgraded ? 7 : 10;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta accumulator = new ActionMeta();
		data.addTrigger(id, Trigger.RECEIVE_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.STRENGTH) || accumulator.getBool()) return TriggerResult.keep();

			accumulator.addCount(ev.getStacks());
			int bonus = accumulator.getCount() / threshold;
			accumulator.setCount(accumulator.getCount() % threshold);
			if (bonus <= 0) return TriggerResult.keep();

			accumulator.setBool(true);
			data.applyStatus(StatusType.STRENGTH, data, bonus * BONUS_STRENGTH, -1, this);
			accumulator.setBool(false);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.HEAVY_CORE, "For every " + GlossaryTag.STRENGTH.tag(this, threshold)
				+ " applied to you, gain " + GlossaryTag.STRENGTH.tag(this, BONUS_STRENGTH)
				+ ". Strength gained from this relic does not count toward this effect.");
	}
}