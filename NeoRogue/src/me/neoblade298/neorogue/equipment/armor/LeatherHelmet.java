package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class LeatherHelmet extends Equipment {
	private double shields;
	
	public LeatherHelmet(boolean isUpgraded) {
		super("leatherHelmet", "Leather Helmet", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ARMOR);
		shields = isUpgraded ? 30 : 20;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addShield(p.getUniqueId(), shields, false, 0, 0, 0, 0);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_HELMET, "Start every fight with <yellow>" + shields + " </yellow>shields.");
	}
}
