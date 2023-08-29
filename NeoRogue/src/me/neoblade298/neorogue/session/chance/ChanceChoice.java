package me.neoblade298.neorogue.session.chance;

import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.session.Session;
import net.md_5.bungee.api.ChatColor;

public class ChanceChoice {
	private String title;
	private Material mat;
	private ChanceStage result;
	private ArrayList<String> desc, prereqFail;
	private ChanceAction action;
	
	public ChanceChoice(Material mat, String title, String description, String prereqFail, ChanceAction action) {
		this(mat, title, description, action);
		this.prereqFail = SharedUtil.addLineBreaks(prereqFail, 250, ChatColor.RED);
	}
	
	public ChanceChoice(Material mat, String title, String description, ChanceAction action) {
		this(mat, title, description);
		this.action = action;
	}
	
	public ChanceChoice(Material mat, String title, String description) {
		this(mat, title);
		this.desc = SharedUtil.addLineBreaks(SharedUtil.translateColors(description), 250, ChatColor.GRAY);
	}
	
	public ChanceChoice(Material mat, String title) {
		this.mat = mat;
		this.title = SharedUtil.translateColors(title);
	}
	
	public ItemStack getItem(Session s) {
		ItemStack item = new ItemStack(mat);
		
		// Check conditions
		boolean canRun = action != null ? action.run(s, false) : true;
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName((canRun ? "§e" : "§c§m") + title);
		ArrayList<String> lore = new ArrayList<String>();
		
		if (desc != null) {
			for (String line : desc) {
				lore.add((canRun ? "" : "§m") + line);
			}
		}
		
		if (!canRun && prereqFail != null) {
			lore.addAll(prereqFail);
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
	
	public void setResult(ChanceStage stage) {
		this.result = stage;
	}
	
	public ChanceStage choose(Session s, ChanceInstance inst) {
		s.broadcastSound(Sound.ENTITY_ARROW_HIT_PLAYER);
		for (Player player : s.getOnlinePlayers()) {
			player.closeInventory();
		}
		if (action != null) action.run(s, true);
		return result;
	}
}
