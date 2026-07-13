package me.neoblade298.neorogue.player.inventory;

import java.util.ArrayList;
import java.util.HashMap;
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
import org.bukkit.inventory.meta.SkullMeta;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.session.instances.LobbyInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class JoinGameInventory extends CoreInventory {
	private static final int BACK = 0;
	// Lobbies occupy the middle row (9-17); started sessions occupy the bottom row (18-26).
	private final HashMap<Integer, Session> lobbySlots = new HashMap<>();
	private final HashMap<Integer, Session> sessionSlots = new HashMap<>();

	public JoinGameInventory(Player p) {
		super(p, Bukkit.createInventory(p, 27, Component.text("Join Game", NamedTextColor.DARK_RED)));
		setupInventory();
	}

	private void setupInventory() {
		p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
		ItemStack[] contents = inv.getContents();
		contents[BACK] = CoreInventory.createButton(Material.BARRIER, Component.text("Back", NamedTextColor.RED));

		int lobbySlot = 9, sessionSlot = 18;
		for (Session session : SessionManager.getSessions()) {
			boolean isLobby = session.getInstance() instanceof LobbyInstance;
			if (isLobby) {
				if (lobbySlot > 17) continue;
				contents[lobbySlot] = buildSessionHead(session, true);
				lobbySlots.put(lobbySlot, session);
				lobbySlot++;
			}
			else {
				if (sessionSlot > 26) continue;
				contents[sessionSlot] = buildSessionHead(session, false);
				sessionSlots.put(sessionSlot, session);
				sessionSlot++;
			}
		}

		if (lobbySlots.isEmpty()) contents[13] = placeholder("No open lobbies");
		if (sessionSlots.isEmpty()) contents[22] = placeholder("No active games");

		inv.setContents(contents);
	}

	private ItemStack buildSessionHead(Session session, boolean isLobby) {
		UUID hostUuid = session.getHost();
		String hostName = Bukkit.getOfflinePlayer(hostUuid).getName();
		ItemStack item = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta meta = (SkullMeta) item.getItemMeta();
		meta.setOwningPlayer(Bukkit.getOfflinePlayer(hostUuid));
		meta.displayName(Component.text(session.getName(), NamedTextColor.GOLD));
		List<Component> lore = new ArrayList<>();
		lore.add(Component.text("Host: " + (hostName != null ? hostName : "Unknown"), NamedTextColor.GRAY));
		lore.add(Component.text("Players: " + session.getParty().size(), NamedTextColor.GRAY));
		lore.add(Component.text("Regions Completed: " + session.getRegionsCompleted(), NamedTextColor.GRAY));
		lore.add(Component.empty());
		if (isLobby) {
			lore.add(Component.text("Open Lobby", NamedTextColor.GREEN));
			lore.add(Component.text("Left click to join", NamedTextColor.GREEN));
			lore.add(Component.text("Right click to spectate", NamedTextColor.YELLOW));
		}
		else {
			lore.add(Component.text("In Progress", NamedTextColor.RED));
			lore.add(Component.text("Click to spectate", NamedTextColor.YELLOW));
		}
		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}

	private ItemStack placeholder(String text) {
		ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
		ItemMeta meta = pane.getItemMeta();
		meta.displayName(Component.text(text, NamedTextColor.GRAY));
		pane.setItemMeta(meta);
		return pane;
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

		if (lobbySlots.containsKey(slot)) {
			Session session = lobbySlots.get(slot);
			p.closeInventory();
			if (e.isRightClick()) SessionManager.trySpectate(p, session);
			else SessionManager.tryJoin(p, session);
			return;
		}
		if (sessionSlots.containsKey(slot)) {
			Session session = sessionSlots.get(slot);
			p.closeInventory();
			SessionManager.trySpectate(p, session);
			return;
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
