package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class RingOfNight extends Equipment {
	private static final String ID = "RingOfNight";
	private int stealth;
	
	public RingOfNight(boolean isUpgraded) {
		super(ID, "Ring of Night", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.ACCESSORY, EquipmentProperties.none());
		stealth = isUpgraded ? 2 : 1;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.applyStatus(StatusType.STEALTH, data, stealth, -1);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLACK_BANNER,
				"Start every fight with " + GlossaryTag.STEALTH.tag(this, stealth, true) + ".");
	}
}
