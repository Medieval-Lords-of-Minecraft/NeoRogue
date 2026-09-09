package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;

public class MechanistManual extends TaggedDropStartingBonus {
	private static final String ID = "MechanistManual";

	public MechanistManual() {
		super(ID, "Mechanist Manual", EquipmentClass.ARCHER, splitStartingBonusWeight(5), Material.KNOWLEDGE_BOOK,
				GlossaryTag.TRAP);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
}