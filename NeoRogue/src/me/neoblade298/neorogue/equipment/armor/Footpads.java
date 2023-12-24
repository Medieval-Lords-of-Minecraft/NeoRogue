package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Armor;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Footpads extends Armor {
	private double stamina;
	
	public Footpads(boolean isUpgraded) {
		super("footpads", "Footpads", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR);
		stamina = isUpgraded ? 3 : 5;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addTrigger(id, Trigger.RECEIVED_DAMAGE, (pdata, in) -> {
			data.addStamina(stamina);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(this, Material.LEATHER_BOOTS, null, "Receiving damage grants you <yellow>" + stamina + "</yellow> stamina.");
	}
}
