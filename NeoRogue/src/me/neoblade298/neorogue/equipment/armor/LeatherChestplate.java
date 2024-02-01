package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class LeatherChestplate extends Equipment {
	private double damageReduction;
	
	public LeatherChestplate(boolean isUpgraded) {
		super("leatherChestplate", "Leather Chestplate", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ARMOR);
		damageReduction = isUpgraded ? 2 : 1;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addBuff(p.getUniqueId(), false, false, BuffType.PHYSICAL, damageReduction);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_CHESTPLATE, "Decrease all " + GlossaryTag.PHYSICAL.tag(this) + " damage by <white>" + damageReduction + "</white>.");
	}
}
