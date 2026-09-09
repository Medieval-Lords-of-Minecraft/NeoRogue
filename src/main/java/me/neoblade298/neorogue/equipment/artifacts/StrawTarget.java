package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;

public class StrawTarget extends TaggedDropStartingBonus {
	private static final String ID = "StrawTarget";

	public StrawTarget() {
		super(ID, "Straw Target", EquipmentClass.ARCHER, splitStartingBonusWeight(5), Material.TARGET,
				GlossaryTag.REND, GlossaryTag.AFTERSHOT);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
}