package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Armor;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.player.Status.StatusType;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class SpikedPauldrons extends Armor {
	private int thorns;
	
	public SpikedPauldrons(boolean isUpgraded) {
		super("spikedPauldrons", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR);
		display = "Leather Helmet";
		thorns = isUpgraded ? 3 : 2;
		item = createItem(this, Material.ARMOR_STAND, null, "Start every fight with <yellow>" + thorns + " </yellow>thorns.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int hotbar) {
		data.applyStatus(StatusType.THORNS, p.getUniqueId(), thorns, -1);
	}
}
