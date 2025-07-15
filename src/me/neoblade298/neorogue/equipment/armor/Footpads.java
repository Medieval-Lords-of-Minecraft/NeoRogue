package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Footpads extends Equipment {
	private static final String ID = "footpads";
	private double stamina;
	
	public Footpads(boolean isUpgraded) {
		super(ID, "Footpads", isUpgraded, Rarity.COMMON, new EquipmentClass[] { EquipmentClass.WARRIOR, EquipmentClass.THIEF },
				EquipmentType.ARMOR);
		stamina = isUpgraded ? 15 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, (pdata, in) -> {
			data.addStamina(stamina);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_BOOTS, "Receiving damage grants you <yellow>" + stamina + "</yellow> stamina.");
	}
}
