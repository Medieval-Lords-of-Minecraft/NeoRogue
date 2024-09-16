package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Ammunition;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageType;

public class WoodenArrow extends Ammunition {
	private static final String ID = "woodenArrow";
	
	public WoodenArrow(boolean isUpgraded) {
		super(ID, "Wooden Arrow", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofAmmunition(isUpgraded ? 7 : 2, 0.1, DamageType.PIERCING));
		properties.addUpgrades(PropertyType.DAMAGE);
	}

	@Override
	public void setupReforges() {
		addSelfReforge(StoneArrow.get(), LitArrow.get(), SerratedArrow.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ARROW);
	}
}
