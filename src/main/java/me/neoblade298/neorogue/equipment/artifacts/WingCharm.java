package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;

public class WingCharm extends TaggedDropStartingBonus {
	private static final String ID = "WingCharm";

	public WingCharm() {
		super(ID, "Wing Charm", EquipmentClass.THIEF, Material.FEATHER, GlossaryTag.EVADE, GlossaryTag.DASH);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
}