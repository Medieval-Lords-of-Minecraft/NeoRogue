package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public abstract class Ability extends Usable {
	protected int manaCost = 0;
	protected int staminaCost = 0;
	protected int range = 0;
	public Ability(String id, boolean isUpgraded, Rarity rarity, EquipmentClass ec) {
		super(id, isUpgraded, rarity, ec);
	}
	
	public ItemStack createItem(Ability a, Material mat, String[] preLoreLine, String loreLine) {
		ArrayList<String> preLore = new ArrayList<String>();
		if (a.manaCost > 0) preLore.add("§6Mana Cost: §e" + a.manaCost);
		if (a.staminaCost > 0) preLore.add("§6Stamina Cost: §e" + a.staminaCost);
		if (a.range > 0) preLore.add("§6Range: §e" + a.range);
		if (a.cooldown > 0) preLore.add("§6Cooldown: §e" + a.cooldown);
		if (preLoreLine != null) {
			for (String l : preLoreLine) {
				preLore.add(l);
			}
		}
		
		return createItem(mat, "Armor", preLore, loreLine, null);
	}
	
	public int getManaCost() {
		return manaCost;
	}
	
	public int getStaminaCost() {
		return staminaCost;
	}
	
	public int getRange() {
		return range;
	}
}
