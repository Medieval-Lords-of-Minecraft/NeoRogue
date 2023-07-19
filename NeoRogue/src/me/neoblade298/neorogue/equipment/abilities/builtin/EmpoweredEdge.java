package me.neoblade298.neorogue.equipment.abilities.builtin;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neorogue.equipment.Ability;

public class EmpoweredEdge extends Ability {
	
	public EmpoweredEdge(boolean upgraded) {
		super("empoweredEdge", Ability.createItem(Material.FLINT, "Empowered Edge",
				new String[] {"&6Cooldown: &e7"}, "&7), upgraded, upgraded ? 5 : 7);
	}

	@Override
	public int getCooldown() {
		// TODO Auto-generated method stub
		return 0;
	}
}
