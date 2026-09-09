package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;

public class LightningInABottle extends TaggedDropStartingBonus {
	private static final String ID = "LightningInABottle";

	public LightningInABottle() {
		super(ID, "Lightning in a Bottle", new EquipmentClass[] { EquipmentClass.THIEF, EquipmentClass.MAGE },
				Material.EXPERIENCE_BOTTLE,
				GlossaryTag.ELECTRIFIED, GlossaryTag.LIGHTNING);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
}