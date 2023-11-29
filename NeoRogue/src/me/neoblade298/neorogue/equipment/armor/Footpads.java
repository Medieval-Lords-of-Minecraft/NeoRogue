package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Armor;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class Footpads extends Armor {
	private double stamina;
	
	public Footpads(boolean isUpgraded) {
		super("footpads", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR);
		display = "Footpads";
		stamina = isUpgraded ? 3 : 5;
		item = createItem(this, Material.LEATHER_BOOTS, null, "Receiving damage grants you <yellow>" + stamina + "</yellow> stamina.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addTrigger(id, Trigger.RECEIVED_DAMAGE, (in) -> {
			data.addStamina(stamina);
			return false;
		});
	}
}
