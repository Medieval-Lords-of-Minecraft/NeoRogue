package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class RingOfParanoia extends Equipment {
	private static final String ID = "RingOfParanoia";
	private static final int INSANITY_PERCENT = 100;
	private final int darkPercent;

	public RingOfParanoia(boolean isUpgraded) {
		super(ID, "Ring of Paranoia", isUpgraded, Rarity.EPIC, EquipmentClass.THIEF, EquipmentType.ACCESSORY);
		darkPercent = isUpgraded ? 50 : 30;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (ev.isStatus(StatusType.INSANITY)) {
				ev.getStacksBuffList().add(Buff.multiplier(data, INSANITY_PERCENT * 0.01,
						BuffStatTracker.statusBuff(id + slot, this)));
			}
			return TriggerResult.keep();
		});
		data.addDamageBuff(DamageBuffType.of(DamageCategory.DARK), Buff.multiplier(data, darkPercent * 0.01,
				BuffStatTracker.damageBuffAlly(id + slot, this)));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ENDER_EYE, "Increase " + GlossaryTag.INSANITY.tag(this) + " stacks applied by "
				+ DescUtil.val(INSANITY_PERCENT + "%") + " and " + GlossaryTag.DARK.tag(this) + " damage dealt by "
				+ DescUtil.val(darkPercent + "%") + ".");
	}
}