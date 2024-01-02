package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ClothBindings extends Equipment {
	private double health;
	
	public ClothBindings(boolean isUpgraded) {
		super("clothBindings", "Cloth Bindings", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ARMOR);
		health = isUpgraded ? 15 : 10;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addTrigger(id, Trigger.WIN_FIGHT, (pdata, in) -> {
			data.addHealth(health);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.WHITE_DYE, "Winning a fight heals you for <yellow>" + health + "</yellow>.");
	}
}
