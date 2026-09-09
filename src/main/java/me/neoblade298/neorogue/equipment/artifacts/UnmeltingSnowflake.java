package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;

public class UnmeltingSnowflake extends TaggedDropStartingBonus {
	private static final String ID = "UnmeltingSnowflake";

	public UnmeltingSnowflake() {
		super(ID, "Unmelting Snowflake", EquipmentClass.ARCHER, splitStartingBonusWeight(5), Material.SNOWBALL,
				GlossaryTag.FROST, GlossaryTag.ICE);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
}