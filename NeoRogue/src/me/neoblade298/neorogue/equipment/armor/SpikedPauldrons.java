package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Armor;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class SpikedPauldrons extends Armor {
	private int thorns;
	
	public SpikedPauldrons(boolean isUpgraded) {
		super("spikedPauldrons", "Spiked Pauldrons", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR);
		thorns = isUpgraded ? 3 : 2;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.applyStatus(StatusType.THORNS, p.getUniqueId(), thorns, -1);
	}

	@Override
	public void setupItem() {
		item = createItem(this, Material.ARMOR_STAND, null, "Start every fight with <yellow>" + thorns + " </yellow>thorns.");
	}
}
