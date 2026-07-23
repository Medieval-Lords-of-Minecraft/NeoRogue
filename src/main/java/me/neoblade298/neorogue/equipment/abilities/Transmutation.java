package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class Transmutation extends Equipment {
	private static final String ID = "Transmutation";

	public Transmutation(boolean isUpgraded) {
		super(ID, "Transmutation", isUpgraded, Rarity.EPIC, EquipmentClass.CLASSLESS,
				EquipmentType.ABILITY, EquipmentProperties.none());
		this.reforgeWildcard = true;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		// Utility item: has no effect during combat.
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHERITE_INGOT,
				"Can reforge any item with reforge options into " + DescUtil.val("any") +
				" of its possible reforge results.");
	}
}
