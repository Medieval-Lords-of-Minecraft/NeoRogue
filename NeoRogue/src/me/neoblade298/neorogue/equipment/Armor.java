package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import me.neoblade298.neocore.shared.util.SharedUtil;

public abstract class Armor extends Equipment {

	public Armor(String id, boolean isUpgraded, Rarity rarity) {
		super(id, isUpgraded, rarity);
		// TODO Auto-generated constructor stub
	}


	public ItemStack createItem(Armor a, Material mat, String[] preLoreLine, String loreLine) {
		ArrayList<String> preLore = new ArrayList<String>();
		// Add stats
		if (preLoreLine != null) {
			for (String l : preLoreLine) {
				preLore.add(SharedUtil.translateColors(l));
			}
		}
		
		return createItem(mat, "Armor", preLore, loreLine, null);
	}
}
