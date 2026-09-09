package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;

public class Kettlebell extends TaggedDropStartingBonus {
	private static final String ID = "Kettlebell";

	public Kettlebell() {
		super(ID, "Kettlebell", EquipmentClass.WARRIOR, Material.HEAVY_CORE, GlossaryTag.STRENGTH);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
}