package me.neoblade298.neorogue.player.inventory;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class SpectateSelectInventory extends CoreInventory {
	private HashMap<Integer, PlayerSessionData> players = new HashMap<Integer, PlayerSessionData>();
	private Session s;

	public SpectateSelectInventory(Session s, Player p) {
		super(p, Bukkit.createInventory(p, 9, Component.text("Choose a player", NamedTextColor.BLUE)));
		this.s = s;
		p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
		int partySize = s.getParty().size();
		if (s.getParty().containsKey(p.getUniqueId())) partySize--;
		int idx = 5 - partySize;
		
		// Display all players that aren't you
		for (PlayerSessionData data : s.getParty().values()) {
			if (p.getUniqueId().equals(data.getUniqueId())) continue;
	        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
	        SkullMeta meta = (SkullMeta) skull.getItemMeta();
	        meta.setOwningPlayer(data.getPlayer() != null ? data.getPlayer() : Bukkit.getOfflinePlayer(data.getUniqueId()));
	        meta.displayName(Component.text(data.getData().getDisplay(), NamedTextColor.RED));
	        skull.setItemMeta(meta);
			inv.setItem(idx, skull);
			players.put(idx, data);
			idx += 2;
		}
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		System.out.println(e.getCurrentItem());
		if (e.getCurrentItem() == null) return;
		
		Instance inst = s.getInstance();
		if (inst instanceof NodeSelectInstance) {
			
		}
	}
}
