package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
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

public class RingOfTheDevastator extends Equipment {
	private static final String ID = "RingOfTheDevastator";
	private static final int BERSERK_THRESHOLD = 5;
	private static final double MANA_REGEN = 0.3;
	private int statusThreshold;

	public RingOfTheDevastator(boolean isUpgraded) {
		super(ID, "Ring of the Devastator", isUpgraded, Rarity.EPIC, EquipmentClass.WARRIOR,
				EquipmentType.ACCESSORY);
		statusThreshold = isUpgraded ? 20 : 30;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta statusProgress = new ActionMeta();
		ActionMeta berserkTiers = new ActionMeta();
		berserkTiers.setInt(getBerserkTiers(data));
		data.addManaRegen(berserkTiers.getInt() * MANA_REGEN);
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.SANCTIFIED) && !ev.isStatus(StatusType.CONCUSSED)) return TriggerResult.keep();
			statusProgress.addCount(ev.getStacks());
			while (statusProgress.getCount() >= statusThreshold) {
				statusProgress.setCount(statusProgress.getCount() - statusThreshold);
				data.applyStatus(StatusType.BERSERK, data, 1, -1, this);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.RECEIVE_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.BERSERK)) return TriggerResult.keep();
			int currentTiers = getBerserkTiers(data);
			data.addManaRegen((currentTiers - berserkTiers.getInt()) * MANA_REGEN);
			berserkTiers.setInt(currentTiers);
			return TriggerResult.keep();
		});
	}

	private int getBerserkTiers(PlayerFightData data) {
		return data.hasStatus(StatusType.BERSERK)
				? data.getStatus(StatusType.BERSERK).getStacks() / BERSERK_THRESHOLD : 0;
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHERITE_SCRAP, "Every " + DescUtil.val(statusThreshold) + " combined "
				+ GlossaryTag.SANCTIFIED.tag(this) + " or " + GlossaryTag.CONCUSSED.tag(this) + " applied grants "
				+ GlossaryTag.BERSERK.tag(this, 1) + ". For every " + GlossaryTag.BERSERK.tag(this, 1)
				+ " you have, gain " + DescUtil.white(MANA_REGEN) + " mana regen.");
	}
}