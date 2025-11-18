package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class MinorStaminaRelic extends Equipment {
	private static final String ID = "MinorStaminaRelic";
	private double regen;
	
	public MinorStaminaRelic(boolean isUpgraded) {
		super(ID, "Minor Stamina Relic", isUpgraded, Rarity.COMMON, 
				new EquipmentClass[] {EquipmentClass.WARRIOR, EquipmentClass.THIEF},
				EquipmentType.ACCESSORY);
		regen = isUpgraded ? 0.8 : 0.5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addStaminaRegen(regen);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLD_NUGGET, "Increases stamina regen by <yellow>" + regen + "</yellow>.");
	}
}
