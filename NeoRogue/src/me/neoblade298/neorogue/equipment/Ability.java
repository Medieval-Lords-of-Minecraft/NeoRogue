package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.shared.util.SharedUtil;
import net.md_5.bungee.api.ChatColor;

public abstract class Ability extends Usable {
	protected int manaCost = 0;
	protected int staminaCost = 0;
	protected int range = 0;
	public Ability(String id, boolean isUpgraded, Rarity rarity) {
		super(id, isUpgraded, rarity);
	}
	
	public static ItemStack createItem(Ability a, Material mat, String displayName, String[] preLoreLine, String loreLine) {
		ItemStack item = new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.RED + displayName + (a.isUpgraded ? "+" : ""));
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("§4Ability");
		
		// Add stats
		if (a.manaCost > 0) lore.add("§6Mana Cost: §e" + a.manaCost);
		if (a.staminaCost > 0) lore.add("§6Stamina Cost: §e" + a.staminaCost);
		if (a.range > 0) lore.add("§6Range: §e" + a.range);
		if (a.cooldown > 0) lore.add("§6Cooldown: §e" + a.cooldown);
		
		if (preLoreLine != null) {
			for (String l : preLoreLine) {
				lore.add(SharedUtil.translateColors(l));
			}
		}
		lore.addAll(SharedUtil.addLineBreaks(SharedUtil.translateColors(loreLine), 200, ChatColor.GRAY));
		meta.setLore(lore);
		item.setItemMeta(meta);
		NBTItem nbti = new NBTItem(item);
		nbti.setString("equipId", a.id);
		nbti.setString("type", "ABILITY");
		nbti.setBoolean("isUpgraded", a.isUpgraded);
		return nbti.getItem();
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
