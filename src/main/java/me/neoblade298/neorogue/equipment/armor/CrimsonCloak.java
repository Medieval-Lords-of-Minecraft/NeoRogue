package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
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

public class CrimsonCloak extends Equipment {
	private static final String ID = "CrimsonCloak";
	private int reduction = 2, statusStacks = 1;

	public CrimsonCloak(boolean isUpgraded) {
		super(ID, "Crimson Cloak", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER, EquipmentType.ARMOR);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta alternating = new ActionMeta();
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.DIRECT),
				Buff.increase(data, reduction, StatTracker.defenseBuffAlly(id + slot, this)));
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent event = (ApplyStatusEvent) in;
			if (!event.isStatus(StatusType.CORRUPTION)) return TriggerResult.keep();
			StatusType status = alternating.getBool() ? StatusType.SHELL : StatusType.PROTECT;
			data.applyStatus(status, data, statusStacks, -1, this);
			alternating.setBool(!alternating.getBool());
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_CHESTPLATE, "Reduce " + GlossaryTag.DIRECT.tag(this)
				+ " damage taken by " + DescUtil.val(reduction) + ". Each time you apply "
				+ GlossaryTag.CORRUPTION.tag(this) + ", apply " + GlossaryTag.PROTECT.tag(this, statusStacks)
				+ " or " + GlossaryTag.SHELL.tag(this, statusStacks) + " to yourself, alternating in order.");
	}
}