package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class SmokyArmguard extends Equipment {
	private static final String ID = "SmokyArmguard";
	private static final int DURATION = 5;
	private int reduction;

	public SmokyArmguard(boolean isUpgraded) {
		super(ID, "Smoky Armguard", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF, EquipmentType.OFFHAND);
		reduction = isUpgraded ? 3 : 2;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.INSANITY)) return TriggerResult.keep();
			data.addDefenseBuff(DamageBuffType.of(DamageCategory.DIRECT), Buff.increase(data, reduction,
					StatTracker.defenseBuffAlly(id + slot, this, false)), DURATION * 20);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GRAY_DYE, "Applying " + GlossaryTag.INSANITY.tag(this) + " grants "
				+ DescUtil.val(reduction) + " " + GlossaryTag.DIRECT.tag(this) + " damage reduction "
				+ DescUtil.duration(DURATION) + ". Refreshes; does not stack.");
	}
}