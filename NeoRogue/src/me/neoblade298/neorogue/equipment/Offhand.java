package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.shared.util.SharedUtil;
import net.md_5.bungee.api.ChatColor;

public abstract class Offhand extends Equipment {

	public Offhand(String id, boolean isUpgraded, Rarity rarity) {
		super(id, isUpgraded, rarity);
		// TODO Auto-generated constructor stub
	}

	public static ItemStack createItem(Offhand o, Material mat, String[] preLoreLine, String loreLine) {
		ItemStack item = new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.RED + o.display + (o.isUpgraded ? "+" : ""));
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("§4Offhand");
		
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
		
		// Set attack speed if weapon is melee
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		meta.setUnbreakable(true);
		item.setItemMeta(meta);
		NBTItem nbti = new NBTItem(item);
		nbti.setString("equipId", o.id);
		nbti.setString("type", "OFFHAND");
		nbti.setBoolean("isUpgraded", o.isUpgraded);
		return nbti.getItem();
	}
}
