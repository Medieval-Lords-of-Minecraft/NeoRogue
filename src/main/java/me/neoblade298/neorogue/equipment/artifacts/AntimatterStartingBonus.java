package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;

public class AntimatterStartingBonus extends TaggedDropStartingBonus {
	private static final String ID = "AntimatterStartingBonus";

	public AntimatterStartingBonus() {
		super(ID, "Antimatter", EquipmentClass.MAGE, Material.END_CRYSTAL, GlossaryTag.RIFT);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
}