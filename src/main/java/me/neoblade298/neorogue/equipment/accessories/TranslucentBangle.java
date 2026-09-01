package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;

public class TranslucentBangle extends Equipment {
	private static final String ID = "TranslucentBangle";
	private static final int MANA_THRESHOLD = 30, DURATION = 12;
	private int protect;

	public TranslucentBangle(boolean isUpgraded) {
		super(ID, "Translucent Bangle", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ACCESSORY, EquipmentProperties.none());
		protect = isUpgraded ? 3 : 2;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta manaSpent = new ActionMeta();
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			CastUsableEvent event = (CastUsableEvent) in;
			if (event.getInstance().getEquipment().getType() != EquipmentType.ABILITY) {
				return TriggerResult.keep();
			}

			manaSpent.addDouble(event.getInstance().getManaCost());
			while (manaSpent.getDouble() >= MANA_THRESHOLD) {
				manaSpent.addDouble(-MANA_THRESHOLD);
				data.applyStatus(StatusType.PROTECT, data, protect, DURATION * 20, this);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CLOCK, GlossaryTag.PASSIVE.tag(this) + ". For every "
				+ DescUtil.val(MANA_THRESHOLD) + " base mana spent casting abilities, gain "
				+ GlossaryTag.PROTECT.tag(this, protect) + " " + DescUtil.duration(DURATION) + ".");
	}
}