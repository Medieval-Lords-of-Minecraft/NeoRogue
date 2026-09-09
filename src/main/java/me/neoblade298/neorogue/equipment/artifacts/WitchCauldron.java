package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;

public class WitchCauldron extends TaggedDropStartingBonus {
	private static final String ID = "WitchCauldron";

	public WitchCauldron() {
		super(ID, "Witch Cauldron", EquipmentClass.THIEF, Material.CAULDRON, GlossaryTag.POISON);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
}