package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public abstract class Ability extends Usable {
	protected int range = 0;
	public Ability(String id, boolean isUpgraded, Rarity rarity, EquipmentClass ec) {
		super(id, isUpgraded, rarity, ec);
	}
	
	protected void setBaseProperties(int cooldown, int manaCost, int staminaCost, int range) {
		super.setBaseProperties(cooldown, manaCost, staminaCost);
		this.range = range;
	}
	
	public ItemStack createItem(Ability a, Material mat, String[] preLoreLine, String loreLine) {
		ArrayList<String> preLore = new ArrayList<String>();
		if (a.manaCost > 0) preLore.add("<gold>Mana Cost: <yellow>" + a.manaCost);
		if (a.staminaCost > 0) preLore.add("<gold>Stamina Cost: <yellow>" + a.staminaCost);
		if (a.range > 0) preLore.add("<gold>Range: <yellow>" + a.range);
		if (a.cooldown > 0) preLore.add("<gold>Cooldown: <yellow>" + a.cooldown);
		if (preLoreLine != null) {
			for (String l : preLoreLine) {
				preLore.add(l);
			}
		}
		
		return createItem(mat, "Armor", preLore, loreLine, null);
	}
	
	public int getRange() {
		return range;
	}
}
