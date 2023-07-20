package me.neoblade298.neorogue.equipment.abilities.builtin;

import org.bukkit.Material;
import me.neoblade298.neorogue.equipment.Ability;
import me.neoblade298.neorogue.equipment.Rarity;

public class BattleCry extends Ability {
	
	public BattleCry(boolean isUpgraded) {
		super("battleCry", isUpgraded, Rarity.COMMON);
		cooldown = 30;
		staminaCost = 25;
		int strength = isUpgraded ? 4 : 3;
		item = Ability.createItem(this, Material.REDSTONE, "Battle Cry",
				null, "&7On cast, give yourself &e" + strength + " &7strength for &e10&7 seconds.");
	}
}
