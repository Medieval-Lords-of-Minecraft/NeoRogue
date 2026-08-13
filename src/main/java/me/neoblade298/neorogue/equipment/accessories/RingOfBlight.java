package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusClass;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class RingOfBlight extends Equipment {
	private static final String ID = "RingOfBlight";
	private static final int CORRUPTION = 2;
	private int statusIncrease;

	public RingOfBlight(boolean isUpgraded) {
		super(ID, "Ring of Blight", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ACCESSORY, EquipmentProperties.none());
		statusIncrease = isUpgraded ? 45 : 30;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.applyStatus(StatusType.CORRUPTION, data, CORRUPTION, -1, this);
		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata, in) -> {
			PreApplyStatusEvent event = (PreApplyStatusEvent) in;
			if (event.getStatusClass() == StatusClass.NEGATIVE) {
				event.getStacksBuffList().add(Buff.multiplier(data, statusIncrease * 0.01,
						BuffStatTracker.statusBuff(id + slot, this)));
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ENDER_EYE, GlossaryTag.PASSIVE.tag(this) + ". Start fights with "
				+ GlossaryTag.CORRUPTION.tag(this, CORRUPTION) + ". Increase negative status stacks you apply by "
				+ DescUtil.val(statusIncrease + "%") + ".");
	}
}