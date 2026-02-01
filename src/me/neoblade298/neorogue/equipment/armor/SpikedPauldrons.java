package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class SpikedPauldrons extends Equipment {
	private static final String ID = "SpikedPauldrons";
	private int thorns;
	
	public SpikedPauldrons(boolean isUpgraded) {
		super(ID, "Spiked Pauldrons", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ARMOR);
		thorns = isUpgraded ? 30 : 20;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.applyStatus(StatusType.THORNS, data, thorns, -1);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ARMOR_STAND, "Start every fight with <yellow>" + thorns + " </yellow>" + GlossaryTag.THORNS.tag(this) + ".");
	}
}
