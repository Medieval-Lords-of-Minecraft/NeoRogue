package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public abstract class Offhand extends Equipment {

	public Offhand(String id, boolean isUpgraded, Rarity rarity, EquipmentClass ec) {
		super(id, isUpgraded, rarity, ec);
		// TODO Auto-generated constructor stub
	}

	public ItemStack createItem(Offhand o, Material mat, String[] preLoreLine, String loreLine) {
		ArrayList<String> preLore = new ArrayList<String>();
		// Add stats
		if (preLoreLine != null) {
			for (String l : preLoreLine) {
				preLore.add(l);
			}
		}
		
		ItemStack item = createItem(mat, "Offhand", preLore, loreLine, null);
		return item;
	}
}
