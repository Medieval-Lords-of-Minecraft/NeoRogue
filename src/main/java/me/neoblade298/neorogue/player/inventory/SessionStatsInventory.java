package me.neoblade298.neorogue.player.inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionStatistics;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class SessionStatsInventory extends CoreInventory {
	public SessionStatsInventory(Player viewer, Session s) {
		super(viewer, Bukkit.createInventory(viewer, 9, Component.text("Session Statistics", NamedTextColor.GOLD)));
		viewer.playSound(viewer, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);

		// Host first, then everyone else sorted by display order
		ArrayList<PlayerSessionData> ordered = new ArrayList<PlayerSessionData>();
		PlayerSessionData host = s.getParty().get(s.getHost());
		if (host != null) ordered.add(host);
		ArrayList<PlayerSessionData> rest = new ArrayList<PlayerSessionData>();
		for (PlayerSessionData data : s.getParty().values()) {
			if (s.getHost().equals(data.getUniqueId())) continue;
			rest.add(data);
		}
		Collections.sort(rest);
		ordered.addAll(rest);

		// Compute the party-wide max per stat so the top holder of each stat can be bolded
		ArrayList<SessionStatistics> allStats = new ArrayList<SessionStatistics>();
		for (PlayerSessionData data : ordered) {
			allStats.add(data.getSessionStats());
		}
		SessionStatistics maxStats = SessionStatistics.max(allStats);

		// Center the heads in the single row
		int idx = Math.max(0, 5 - ordered.size());
		for (PlayerSessionData data : ordered) {
			if (idx >= inv.getSize()) break;
			inv.setItem(idx, createStatsHead(s, data, maxStats));
			idx += 2;
		}
	}

	private ItemStack createStatsHead(Session s, PlayerSessionData data, SessionStatistics maxStats) {
		ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta meta = (SkullMeta) skull.getItemMeta();
		meta.setOwningPlayer(data.getPlayer() != null ? data.getPlayer() : Bukkit.getOfflinePlayer(data.getUniqueId()));

		String name = data.getData().getDisplay();
		if (s.getHost().equals(data.getUniqueId())) name = "\u2605 " + name;
		meta.displayName(Component.text(name, NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, State.FALSE));

		List<Component> lore = data.getSessionStats().buildLore(maxStats);
		meta.lore(lore);
		skull.setItemMeta(meta);
		return skull;
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}
}
