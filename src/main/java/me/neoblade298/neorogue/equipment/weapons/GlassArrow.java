package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.LimitedAmmunition;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageType;

public class GlassArrow extends LimitedAmmunition {
	private static final String ID = "GlassArrow";
	
	public GlassArrow(boolean isUpgraded) {
		super(ID, "Glass Arrow", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofAmmunition(40, 0.2, DamageType.PIERCING), isUpgraded ? 15 : 10);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ARROW, "Limited to " + DescUtil.yellow(uses) + " uses per fight.");
	}
}
