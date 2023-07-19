package me.neoblade298.neorogue.equipment.abilities.builtin;

import org.bukkit.Material;
import me.neoblade298.neorogue.equipment.Ability;
import me.neoblade298.neorogue.equipment.Rarity;

public class EmpoweredEdge extends Ability {
	
	public EmpoweredEdge(boolean isUpgraded) {
		super("empoweredEdge", isUpgraded, Rarity.COMMON);
		cooldown = isUpgraded ? 5 : 7;
		int damage = isUpgraded ? 100 : 75;
		item = Ability.createItem(this, Material.FLINT, "Empowered Edge",
				null, "&7On cast, your next basic attack deals &e" + damage + " &7damage.");
	}
}
