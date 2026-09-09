package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;

public class SmellingSalts extends TaggedDropStartingBonus {
	private static final String ID = "SmellingSalts";

	public SmellingSalts() {
		super(ID, "Smelling Salts", EquipmentClass.WARRIOR, Material.SUGAR, GlossaryTag.BERSERK);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
}