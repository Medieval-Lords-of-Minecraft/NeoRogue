package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class HuntingCharm extends Equipment {
	private static final String ID = "HuntingCharm";
	private int focus;

	public HuntingCharm(boolean isUpgraded) {
		super(ID, "Hunting Charm", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER, EquipmentType.ACCESSORY);
		focus = isUpgraded ? 2 : 1;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.applyStatus(StatusType.FOCUS, data, focus, -1, this);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RABBIT_FOOT, "Start fights with " + GlossaryTag.FOCUS.tag(this, focus) + ".");
	}
}