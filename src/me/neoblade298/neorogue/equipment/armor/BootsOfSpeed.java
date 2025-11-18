package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class BootsOfSpeed extends Equipment {
	private static final String ID = "BootsOfSpeed";
	private double stamina;
	
	public BootsOfSpeed(boolean isUpgraded) {
		super(ID, "Boots of Speed", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ARMOR);
		stamina = isUpgraded ? 1.5 : 1;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSprintCost(-stamina);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_BOOTS, "Reduces sprint stamina cost by <yellow>" + stamina + "</yellow>.");
	}
}
