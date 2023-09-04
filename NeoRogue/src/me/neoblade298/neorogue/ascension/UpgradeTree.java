package me.neoblade298.neorogue.ascension;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.player.PlayerData;
import net.md_5.bungee.api.ChatColor;

public abstract class UpgradeTree {
	private static HashMap<String, UpgradeTree> trees = new HashMap<String, UpgradeTree>();
	
	private String id, display, description;
	private Material mat;
	private int slot;
	protected HashMap<Integer, UpgradeHolder> upgrades = new HashMap<Integer, UpgradeHolder>();
	private UpgradeRequirement req;
	private boolean unlocked;
	
	public UpgradeTree(String id, String display, String description, int slot, PlayerData data) {
		this.id = id;
		this.display = display;
		this.description = description;
		this.slot = slot;
		
		unlocked = data.hasUpgrade(id);
		
		trees.put(id, this);
	}
	
	public boolean passesRequirement(PlayerData data) {
		return req == null || req.passesRequirement(data);
	}
	
	public static HashMap<String, UpgradeTree> getTrees() {
		return trees;
	}
	
	public String getDisplay() {
		return display;
	}
	
	public static UpgradeTree get(String id) {
		return trees.get(id);
	}
	
	public int getSlot() {
		return slot;
	}
	
	public HashMap<Integer, UpgradeHolder> getUpgrades() {
		return upgrades;
	}
	
	public ItemStack getIcon(PlayerData data) {
		ItemStack item = new ItemStack(unlocked ? mat : Material.BARRIER);
		ItemMeta meta = item.getItemMeta();
		ChatColor c = unlocked ? ChatColor.GREEN : ChatColor.RED;
		meta.setDisplayName(c + display);
		meta.setLore(SharedUtil.addLineBreaks(description, 250, ChatColor.GRAY));
		item.setItemMeta(meta);
		NBTItem nbti = new NBTItem(item);
		nbti.setString("id", id);
		return nbti.getItem();
	}
}
