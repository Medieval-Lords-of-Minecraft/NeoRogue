package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Armor;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.BuffType;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class LeatherChestplate extends Armor {
	private double damageReduction;
	
	public LeatherChestplate(boolean isUpgraded) {
		super("leatherChestplate", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR);
		display = "Leather Chestplate";
		damageReduction = isUpgraded ? 2 : 1;
		item = createItem(this, Material.LEATHER_CHESTPLATE, null, "Decrease all physical damage by <yellow>" + damageReduction + ".");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addBuff(p.getUniqueId(), false, false, BuffType.PHYSICAL, damageReduction);
	}
}
