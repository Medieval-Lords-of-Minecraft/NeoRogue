package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class LeatherHelmet extends Equipment {
	private static final String ID = "LeatherHelmet";
	private int shields;
	
	public LeatherHelmet(boolean isUpgraded) {
		super(ID, "Leather Helmet", isUpgraded, Rarity.COMMON, EquipmentClass.CLASSLESS,
				EquipmentType.ARMOR);
		shields = isUpgraded ? 15 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addPermanentShield(p.getUniqueId(), shields);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_HELMET, "Start every fight with <yellow>" + shields + " </yellow>" + GlossaryTag.SHIELDS.tag(this) + ".");
	}
}
