package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class MinorManaRelic extends Equipment {
	private double regen;
	
	public MinorManaRelic(boolean isUpgraded) {
		super("minorManaRelic", "Minor Mana Relic", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ACCESSORY);
		regen = isUpgraded ? 2 : 1;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addStaminaRegen(regen);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_NUGGET, "Increases mana regen by <yellow>" + regen + "</yellow>.");
	}
}
