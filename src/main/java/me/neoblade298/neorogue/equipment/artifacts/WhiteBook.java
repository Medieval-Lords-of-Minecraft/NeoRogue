package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;

public class WhiteBook extends TaggedDropStartingBonus {
	private static final String ID = "WhiteBook";

	public WhiteBook() {
		super(ID, "White Book", EquipmentClass.WARRIOR, Material.BOOK, GlossaryTag.SANCTIFIED);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
}