package me.neoblade298.neorogue.player.inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class SpectateMenuInventory extends CoreInventory {
	private static final int BACK = 0;
	private final List<Session> sessions = new ArrayList<>();

	public SpectateMenuInventory(Player p) {
		super(p, Bukkit.createInventory(p, 27, Component.text("Spectate", NamedTextColor.DARK_RED)));
		setupInventory();
	}

	private void setupInventory() {
		p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
		Collection<Session> activeSessions = SessionManager.getSessions();
		ItemStack[] contents = inv.getContents();
		contents[BACK] = CoreInventory.createButton(Material.BARRIER, Component.text("Back", NamedTextColor.RED));

		int slot = 2;
		for (Session session : activeSessions) {
			if (slot >= 27) break;
			sessions.add(session);
			UUID hostUuid = session.getHost();
			String hostName = Bukkit.getOfflinePlayer(hostUuid).getName();
			int partySize = session.getParty().size();
			int regionsCompleted = session.getRegionsCompleted();

			ItemStack item = new ItemStack(Material.SPYGLASS);
			ItemMeta meta = item.getItemMeta();
			meta.displayName(Component.text(session.getName(), NamedTextColor.GOLD));
			List<Component> lore = new ArrayList<>();
			lore.add(Component.text("Host: " + (hostName != null ? hostName : "Unknown"), NamedTextColor.GRAY));
			lore.add(Component.text("Players: " + partySize, NamedTextColor.GRAY));
			lore.add(Component.text("Regions Completed: " + regionsCompleted, NamedTextColor.GRAY));
			lore.add(Component.empty());
			lore.add(Component.text("Click to spectate", NamedTextColor.YELLOW));
			meta.lore(lore);
			item.setItemMeta(meta);
			contents[slot] = item;
			slot++;
		}

		if (sessions.isEmpty()) {
			ItemStack empty = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
			ItemMeta meta = empty.getItemMeta();
			meta.displayName(Component.text("No active sessions", NamedTextColor.GRAY));
			empty.setItemMeta(meta);
			contents[13] = empty;
		}

		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getClickedInventory() == null || e.getClickedInventory().getType() != InventoryType.CHEST) return;
		if (e.getCurrentItem() == null) return;

		int slot = e.getSlot();
		if (slot == BACK) {
			new MainMenuInventory(p);
			return;
		}

		int index = slot - 2;
		if (index >= 0 && index < sessions.size()) {
			Session session = sessions.get(index);
			p.closeInventory();
			session.addSpectator(p);
		}
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}
}
