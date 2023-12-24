package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import me.neoblade298.neorogue.session.fight.DamageType;

public abstract class Weapon extends AbstractWeapon {
	protected double damage, attackSpeed;
	protected DamageType type;

	public Weapon(String id, String display, boolean isUpgraded, Rarity rarity, EquipmentClass ec) {
		super(id, display, isUpgraded, rarity, ec);
		this.equipSlot = EquipSlot.HOTBAR;
	}

	public ItemStack createItem(Material mat, String[] preLoreLine, String loreLine) {
		ArrayList<String> preLore = new ArrayList<String>();
		// Add stats
		if (preLoreLine != null) {
			for (String l : preLoreLine) {
				preLore.add(l);
			}
		}
		
		ItemStack item = createItem(mat, preLoreLine, loreLine, true);
		return item;
	}
}
