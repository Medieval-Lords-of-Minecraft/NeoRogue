package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Armor;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class LeatherChestplate extends Armor {
	private double damageReduction;
	
	public LeatherChestplate(boolean isUpgraded) {
		super("leatherChestplate", "Leather Chestplate", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR);
		damageReduction = isUpgraded ? 2 : 1;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addBuff(p.getUniqueId(), false, false, BuffType.PHYSICAL, damageReduction);
	}

	@Override
	public void setupItem() {
		item = createItem(this, Material.LEATHER_CHESTPLATE, null, "Decrease all physical damage by <yellow>" + damageReduction + ".");
	}
}
