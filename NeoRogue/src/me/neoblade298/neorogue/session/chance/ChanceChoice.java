package me.neoblade298.neorogue.session.chance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import net.md_5.bungee.api.ChatColor;

public class ChanceChoice {
	private String title;
	private Material mat;
	private ArrayList<String> description, prereqFail;
	private ChanceAction action;
	
	public ChanceChoice(Material mat, String title, String description, String prereqFail, ChanceAction action) {
		this.title = title;
		this.description = SharedUtil.addLineBreaks(description, 250, ChatColor.GRAY);
		this.prereqFail = SharedUtil.addLineBreaks(prereqFail, 250, ChatColor.RED);
		this.action = action;
	}
	
	public ItemStack getItem(Session s) {
		ItemStack item = new ItemStack(mat);
		
		// Check conditions
		boolean canRun = action.run(s, false);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName((canRun ? "§7" : "§c§m") + title);
		ArrayList<String> lore = new ArrayList<String>();
		
		for (String line : description) {
			lore.add((canRun ? "" : "§m") + line);
		}
		
		if (!canRun) {
			lore.addAll(prereqFail);
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
	
	public void choose(Session s) {
		action.run(s, true);
	}
}
