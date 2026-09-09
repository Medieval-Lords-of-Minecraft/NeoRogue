package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;

public class CobblestoneSapling extends TaggedDropStartingBonus {
	private static final String ID = "CobblestoneSapling";

	public CobblestoneSapling() {
		super(ID, "Cobblestone Sapling", EquipmentClass.WARRIOR, Material.DEAD_BUSH, GlossaryTag.CONCUSSED);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
}