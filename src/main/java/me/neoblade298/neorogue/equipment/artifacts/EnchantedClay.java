package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;

public class EnchantedClay extends TaggedDropStartingBonus {
	private static final String ID = "EnchantedClay";

	public EnchantedClay() {
		super(ID, "Enchanted Clay", EquipmentClass.MAGE, Material.CLAY_BALL, GlossaryTag.CONCUSSED,
				GlossaryTag.EARTHEN);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
}