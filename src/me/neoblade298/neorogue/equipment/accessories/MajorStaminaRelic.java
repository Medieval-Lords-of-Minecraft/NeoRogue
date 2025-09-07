package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class MajorStaminaRelic extends Equipment {
	private static final String ID = "majorStaminaRelic";
	private double regen, max;
	
	public MajorStaminaRelic(boolean isUpgraded) {
		super(ID, "Major Stamina Relic", isUpgraded, Rarity.RARE, 
				new EquipmentClass[] {EquipmentClass.WARRIOR, EquipmentClass.THIEF},
				EquipmentType.ACCESSORY);
		regen = isUpgraded ? 1.5 : 1;
		max = isUpgraded ? 50 : 30;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addStaminaRegen(regen);
		data.addMaxStamina(max);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLD_NUGGET, "Increases stamina regen by <yellow>" + regen + "</yellow> and max stamina by <yellow>" + max + "</yellow>.");
	}
}
