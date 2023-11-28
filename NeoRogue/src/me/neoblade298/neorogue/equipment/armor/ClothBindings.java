package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Armor;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class ClothBindings extends Armor {
	private double health;
	
	public ClothBindings(boolean isUpgraded) {
		super("clothBindings", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR);
		display = "Cloth Bindings";
		health = isUpgraded ? 10 : 7;
		item = createItem(this, Material.WHITE_DYE, null, "Winning a fight heals you for <yellow>" + health + "</yellow>.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int hotbar) {
		data.addTrigger(id, Trigger.WIN_FIGHT, (in) -> {
			data.addHealth(health);
			return true;
		});
	}
}
