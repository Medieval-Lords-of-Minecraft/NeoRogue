package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neocore.shared.util.SharedUtil;
import net.md_5.bungee.api.ChatColor;

public abstract class Ability extends Usable {
	public Ability(String id, ItemStack item) {
		super(id, item);
	}
	
	public static ItemStack createItem(Material mat, String displayName, String[] preLoreLine, String loreLine) {
		ItemStack item = new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.RED + displayName);
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("§4Ability");
		for (String l : preLoreLine) {
			lore.add(SharedUtil.translateColors(l));
		}
		lore.addAll(SharedUtil.addLineBreaks(SharedUtil.translateColors(loreLine), 250, ChatColor.GRAY));
		meta.setLore(lore);
		return item;
	}
}
