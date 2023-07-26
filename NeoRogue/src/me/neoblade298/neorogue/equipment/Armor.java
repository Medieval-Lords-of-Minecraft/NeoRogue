package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.shared.util.SharedUtil;
import net.md_5.bungee.api.ChatColor;

public abstract class Armor extends Equipment {

	public Armor(String id, boolean isUpgraded, Rarity rarity) {
		super(id, isUpgraded, rarity);
		// TODO Auto-generated constructor stub
	}


	public static ItemStack createItem(Armor a, Material mat, String[] preLoreLine, String loreLine) {
		ArrayList<String> preLore = new ArrayList<String>();
		// Add stats
		if (preLoreLine != null) {
			for (String l : preLoreLine) {
				preLore.add(SharedUtil.translateColors(l));
			}
		}
		
		return Equipment.createItem(a, mat, "Armor", preLore, loreLine, null);
	}
}
