package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;
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
		ItemStack item = new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.RED + a.display + (a.isUpgraded ? "+" : ""));
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("§4Armor");
		
		// Add stats
		
		if (preLoreLine != null) {
			for (String l : preLoreLine) {
				lore.add(SharedUtil.translateColors(l));
			}
		}
		
		if (loreLine != null) {
			lore.addAll(SharedUtil.addLineBreaks(SharedUtil.translateColors(loreLine), 200, ChatColor.GRAY));
		}
		meta.setLore(lore);
		
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		meta.setUnbreakable(true);
		item.setItemMeta(meta);
		NBTItem nbti = new NBTItem(item);
		nbti.setString("equipId", a.id);
		nbti.setString("type", "ARMOR");
		nbti.setBoolean("isUpgraded", a.isUpgraded);
		return nbti.getItem();
	}
}
