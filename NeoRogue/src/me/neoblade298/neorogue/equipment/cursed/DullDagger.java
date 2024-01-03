package me.neoblade298.neorogue.equipment.cursed;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class DullDagger extends Equipment {
	
	public DullDagger() {
		super("dullDagger", "Dull Dagger", EquipmentType.WEAPON);
		// addReforgeOption("leatherGauntlets", new String[] {"forcefulLeatherGauntlets", "earthenLeatherGauntlets", "lightLeatherGauntlets"});
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {}

	@Override
	public void setupItem() {
		item = createItem(Material.BARRIER);
	}
}
