package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class IronCuirass extends Equipment {
	private static final String ID = "ironCuirass";
	private int damageReduction;
	
	public IronCuirass(boolean isUpgraded) {
		super(ID, "Iron Cuirass", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ARMOR);
		damageReduction = isUpgraded ? 8 : 5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addBuff(data, false, false, BuffType.PHYSICAL, damageReduction);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_CHESTPLATE, "Decrease all " + GlossaryTag.PHYSICAL.tag(this) + " damage by <yellow>" + damageReduction + "</yellow>.");
	}
}
