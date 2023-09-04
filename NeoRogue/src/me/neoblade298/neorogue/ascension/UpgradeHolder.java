package me.neoblade298.neorogue.ascension;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.player.PlayerData;
import net.md_5.bungee.api.ChatColor;

public class UpgradeHolder {
	private String id, display, description;
	private UpgradeRequirement req;
	private int slot;
	private Material mat;
	private boolean has, canGet;
	
	public UpgradeHolder(PlayerData data) {
		has = data.hasUpgrade(id);
		canGet = has || req.passesRequirement(data);
	}
	
	public void onClick(Player p, PlayerData data) {
		if (canGet && !has) {
			p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 1F);
			data.addUpgrade(id);
		}
	}
	
	public ItemStack getIcon(PlayerData data) {
		ItemStack item = new ItemStack(canGet ? mat : Material.BARRIER);
		ItemMeta meta = item.getItemMeta();
		ChatColor c = data.hasUpgrade(id) ? ChatColor.GREEN : (canGet ? ChatColor.WHITE : ChatColor.RED);
		meta.setDisplayName(c + display + (has ? " (Owned)" : ""));
		meta.setLore(SharedUtil.addLineBreaks(description, 250, ChatColor.GRAY));
		item.setItemMeta(meta);
		return item;
	}
	
	public void updateItem(ItemStack item, PlayerData data) {
		has = data.hasUpgrade(id);
		canGet = has || req.passesRequirement(data);
		if (item.getType() == Material.BARRIER && canGet) {
			item.setType(mat);
		}
		ItemMeta meta = item.getItemMeta();
		ChatColor c = data.hasUpgrade(id) ? ChatColor.GREEN : (canGet ? ChatColor.WHITE : ChatColor.RED);
		meta.setDisplayName(c + display + (has ? " (Owned)" : ""));
		item.setItemMeta(meta);
	}
}
