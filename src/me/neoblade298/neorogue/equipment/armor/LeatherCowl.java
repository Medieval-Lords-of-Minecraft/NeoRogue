package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class LeatherCowl extends Equipment {
	private static final String ID = "LeatherCowl";
	private int evade;
	
	public LeatherCowl(boolean isUpgraded) {
		super(ID, "Leather Cowl", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ARMOR);
		evade = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.applyStatus(StatusType.EVADE, data, evade, -1);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_HELMET, "Start every fight with <yellow>" + evade + " </yellow>" + GlossaryTag.EVADE.tag(this) + ".");
	}
}
