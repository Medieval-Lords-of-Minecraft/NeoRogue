package me.neoblade298.neorogue.equipment.armor;

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
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusClass;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveDamageEvent;

public class MonarchOfShadows extends Equipment {
	private static final String ID = "MonarchOfShadows";
	private static final int BASE_REDUCTION = 3, STEALTH_THRESHOLD = 3;
	private double statusMultiplier;
	private int statusPercent;

	public MonarchOfShadows(boolean isUpgraded) {
		super(ID, "Monarch of Shadows", isUpgraded, Rarity.EPIC, EquipmentClass.THIEF, EquipmentType.ARMOR);
		statusPercent = isUpgraded ? 150 : 100;
		statusMultiplier = statusPercent * 0.01;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, (pdata, in) -> {
			ReceiveDamageEvent ev = (ReceiveDamageEvent) in;
			int reduction = hasThreeStealth(data) ? BASE_REDUCTION * 2 : BASE_REDUCTION;
			ev.getMeta().addDefenseBuff(DamageBuffType.of(DamageCategory.DIRECT), Buff.increase(data, reduction,
					StatTracker.defenseBuffAlly(id + slot, this, false)));
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (hasThreeStealth(data) && ev.getStatusClass() == StatusClass.NEGATIVE) {
				ev.getStacksBuffList().add(Buff.multiplier(data, statusMultiplier,
						BuffStatTracker.statusBuff(id + slot, this)));
			}
			return TriggerResult.keep();
		});
	}

	private boolean hasThreeStealth(PlayerFightData data) {
		return data.hasStatus(StatusType.STEALTH) && data.getStatus(StatusType.STEALTH).getStacks() >= STEALTH_THRESHOLD;
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHERITE_CHESTPLATE, "Reduce " + GlossaryTag.DIRECT.tag(this) + " damage by "
				+ DescUtil.val(BASE_REDUCTION) + ". At " + GlossaryTag.STEALTH.tag(this, STEALTH_THRESHOLD)
				+ ", double this reduction and increase all negative status applications by "
				+ DescUtil.val(statusPercent + "%") + ".");
	}
}