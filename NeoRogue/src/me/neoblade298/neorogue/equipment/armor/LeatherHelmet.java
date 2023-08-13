package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Armor;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.PlayerFightData;
import me.neoblade298.neorogue.session.fights.Shield;

public class LeatherHelmet extends Armor {
	private double shields;
	
	public LeatherHelmet(boolean isUpgraded) {
		super("leatherHelmet", isUpgraded, Rarity.COMMON, EquipmentClass.SWORDSMAN);
		display = "Leather Helmet";
		shields = isUpgraded ? 30 : 20;
		item = createItem(this, Material.LEATHER_HELMET, null, "&7Start every fight with &e" + shields + " &7permanent shields.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int hotbar) {
		data.getShields().addShield(new Shield(data, p.getUniqueId(), shields, false, 0, 0, 0, 0));
	}
}
