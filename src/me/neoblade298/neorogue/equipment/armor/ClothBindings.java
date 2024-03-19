package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ClothBindings extends Equipment {
	private static final String ID = "clothBindings";
	private double health;
	
	public ClothBindings(boolean isUpgraded) {
		super(ID, "Cloth Bindings", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ARMOR);
		health = isUpgraded ? 20 : 14;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.WIN_FIGHT, (pdata, in) -> {
			FightInstance.giveHeal(p, health, p);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.WHITE_DYE, "Winning a fight heals you for <yellow>" + health + "</yellow>.");
	}
}
