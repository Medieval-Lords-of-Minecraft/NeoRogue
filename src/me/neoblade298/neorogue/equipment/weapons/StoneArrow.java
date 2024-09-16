package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Ammunition;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;

public class StoneArrow extends Ammunition {
	private static final String ID = "stoneArrow";
	private static final int thres = 5;

	private int damage;
	
	public StoneArrow(boolean isUpgraded) {
		super(ID, "Stone Arrow", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofAmmunition(15, 0.1, DamageType.PIERCING));
				damage = isUpgraded ? 15 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ARROW, "Deals an additional " + GlossaryTag.PIERCING.tag(this, damage, true) + " if it travels at least " + DescUtil.white(thres) + " blocks.");
	}
}
