package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class MajorStrengthRelic extends Equipment {
	private static final String ID = "MajorStrengthRelic";
	private double multiplier;
	private int multiplierPercent;

	public MajorStrengthRelic(boolean isUpgraded) {
		super(ID, "Major Strength Relic", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ACCESSORY);
		multiplier = isUpgraded ? 0.3 : 0.2;
		multiplierPercent = (int) (multiplier * 100);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.PRE_RECEIVE_STATUS, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.STRENGTH) || ev.getStacks() <= 0) return TriggerResult.keep();
			ev.getStacksBuffList().add(Buff.multiplier(data, multiplier,
					BuffStatTracker.statusBuff(id + slot, this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.HEAVY_CORE, "Increase all " + GlossaryTag.STRENGTH.tag(this)
				+ " gained by " + DescUtil.val(multiplierPercent + "%") + ".");
	}
}