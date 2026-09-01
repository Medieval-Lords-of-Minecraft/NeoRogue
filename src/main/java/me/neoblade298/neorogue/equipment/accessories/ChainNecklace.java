package me.neoblade298.neorogue.equipment.accessories;

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

public class ChainNecklace extends Equipment {
	private static final String ID = "ChainNecklace";
	private int applications, damagePercent;
	private double damageMultiplier;

	public ChainNecklace(boolean isUpgraded) {
		super(ID, "Chain Necklace", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR, EquipmentType.ACCESSORY);
		applications = isUpgraded ? 3 : 5;
		damagePercent = 20;
		damageMultiplier = damagePercent * 0.01;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta count = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.CONCUSSED)) return TriggerResult.keep();
			if (count.addCount(1) < applications) return TriggerResult.keep();
			data.addDamageBuff(DamageBuffType.of(DamageCategory.PHYSICAL),
					Buff.multiplier(data, damageMultiplier, StatTracker.damageBuffAlly(count.getId(), this)));
			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEAD, "After applying " + GlossaryTag.CONCUSSED.tag(this) + " "
				+ DescUtil.val(applications) + " times in a fight, increase your " + GlossaryTag.PHYSICAL.tag(this)
				+ " damage by " + DescUtil.val(damagePercent + "%") + " for the rest of the fight.");
	}
}