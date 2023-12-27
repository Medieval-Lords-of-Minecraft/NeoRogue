package me.neoblade298.neorogue.equipment.cursed;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class GnarledWand extends Equipment {
	
	public GnarledWand() {
		super("gnarledWand", "Gnarled Wand", EquipmentType.WEAPON);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {}

	@Override
	public void setupItem() {
		item = createItem(Material.BARRIER);
	}
}
