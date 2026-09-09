package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;

public class HuntersCharm extends TaggedDropStartingBonus {
	private static final String ID = "HuntersCharm";

	public HuntersCharm() {
		super(ID, "Hunter's Charm", EquipmentClass.ARCHER, splitStartingBonusWeight(5), Material.RABBIT_FOOT,
				GlossaryTag.FOCUS);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
}