package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Armor;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class LeatherHelmet extends Armor {
	private double shields;
	
	public LeatherHelmet(boolean isUpgraded) {
		super("leatherHelmet", "Leather Helmet", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR);
		shields = isUpgraded ? 30 : 20;
		item = createItem(this, Material.LEATHER_HELMET, null, "Start every fight with <yellow>" + shields + " </yellow>shields.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addShield(p.getUniqueId(), shields, false, 0, 0, 0, 0);
	}
}
