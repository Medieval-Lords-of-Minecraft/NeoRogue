package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;

public class CurseMarkStartingBonus extends TaggedDropStartingBonus {
	private static final String ID = "CurseMarkStartingBonus";

	public CurseMarkStartingBonus() {
		super(ID, "Curse Mark", EquipmentClass.MAGE, Material.WRITABLE_BOOK, GlossaryTag.CORRUPTION,
				GlossaryTag.BURN);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
}