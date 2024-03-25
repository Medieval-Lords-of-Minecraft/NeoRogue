package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class ScaleCape extends Equipment {
	private static final String ID = "scaleCape";
	private int damageReduction;
	
	public ScaleCape(boolean isUpgraded) {
		super(ID, "Scale Cape", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.ARMOR);
		damageReduction = isUpgraded ? 8 : 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addBuff(data, false, false, BuffType.FIRE, damageReduction);
	}

	@Override
	public void setupItem() {
		item = createItem(
				Material.LEATHER_CHESTPLATE,
				"Decrease all " + GlossaryTag.FIRE.tag(this) + " damage by <yellow>" + damageReduction + "</yellow>."
		);
	}
}
