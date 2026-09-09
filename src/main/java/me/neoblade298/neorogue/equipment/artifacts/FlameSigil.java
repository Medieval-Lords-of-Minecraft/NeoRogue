package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;

public class FlameSigil extends TaggedDropStartingBonus {
	private static final String ID = "FlameSigil";

	public FlameSigil() {
		super(ID, "Flame Sigil", EquipmentClass.ARCHER, splitStartingBonusWeight(5), Material.FIRE_CHARGE,
				GlossaryTag.BURN, GlossaryTag.FIRE);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
}