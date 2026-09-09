package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;

public class BlackRose extends TaggedDropStartingBonus {
	private static final String ID = "BlackRose";

	public BlackRose() {
		super(ID, "Black Rose", EquipmentClass.THIEF, Material.WITHER_ROSE, GlossaryTag.DARK,
				GlossaryTag.INSANITY);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
}