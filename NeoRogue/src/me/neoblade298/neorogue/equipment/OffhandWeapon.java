package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public abstract class OffhandWeapon extends AbstractWeapon {

	public OffhandWeapon(String id, String display, boolean isUpgraded, Rarity rarity, EquipmentClass ec) {
		super(id, display, isUpgraded, rarity, ec);
	}

	public ItemStack createItem(Material mat, String[] preLoreLine, String loreLine) {
		ArrayList<String> preLore = new ArrayList<String>();
		// Add stats
		if (preLoreLine != null) {
			for (String l : preLoreLine) {
				preLore.add(l);
			}
		}
		
		ItemStack item = createItem(mat, preLoreLine, loreLine, false);
		return item;
	}
}
