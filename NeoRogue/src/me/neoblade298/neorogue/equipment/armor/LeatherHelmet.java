package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Armor;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Weapon;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.DamageType;
import me.neoblade298.neorogue.session.fights.FightData;
import me.neoblade298.neorogue.session.fights.FightInstance;
import me.neoblade298.neorogue.session.fights.Shield;

public class LeatherHelmet extends Armor {
	private double shields;
	
	public LeatherHelmet(boolean isUpgraded) {
		super("leatherHelmet", isUpgraded, Rarity.COMMON);
		display = "Wooden Sword";
		shields = isUpgraded ? 30 : 20;
		item = Armor.createItem(this, Material.LEATHER_HELMET, null, "&7Start every fight with &e" + shields + " &7permanent shields.");
	}

	@Override
	public void initialize(Player p, FightData data, Trigger bind) {
		data.getShields().addShield(new Shield(data, shields, false, 0, 0, 0, 0));
	}
}
