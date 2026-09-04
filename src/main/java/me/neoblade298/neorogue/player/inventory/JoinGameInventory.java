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
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.session.instances.LobbyInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class JoinGameInventory extends CoreInventory {
	private static final int BACK = 0;
	private static final int LOBBY_PREVIOUS = 2, LOBBY_NEXT = 3;
	private static final int SESSION_PREVIOUS = 5, SESSION_NEXT = 6;
	private static final int PAGE_SIZE = 9;
	// Lobbies occupy the middle row (9-17); started sessions occupy the bottom row (18-26).
	private final HashMap<Integer, Session> lobbySlots = new HashMap<>();
	private final HashMap<Integer, Session> sessionSlots = new HashMap<>();
	private int lobbyPage;
	private int sessionPage;

	public JoinGameInventory(Player p) {
		super(p, Bukkit.createInventory(p, 27, Component.text("Join Game", NamedTextColor.DARK_RED)));
		setupInventory();
	}

	private void setupInventory() {
		p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
		inv.clear();
		lobbySlots.clear();
		sessionSlots.clear();
		ItemStack[] contents = new ItemStack[inv.getSize()];
		contents[BACK] = CoreInventory.createButton(Material.BARRIER, Component.text("Back", NamedTextColor.RED));

		List<Session> lobbies = new ArrayList<>();
		List<Session> sessions = new ArrayList<>();
		for (Session session : SessionManager.getSessions()) {
			if (session.getInstance() instanceof LobbyInstance) lobbies.add(session);
			else sessions.add(session);
		}

		lobbyPage = clampPage(lobbyPage, lobbies.size());
		sessionPage = clampPage(sessionPage, sessions.size());
		fillPage(contents, lobbies, lobbyPage, 9, true, lobbySlots);
		fillPage(contents, sessions, sessionPage, 18, false, sessionSlots);

		int lobbyPages = totalPages(lobbies.size());
		if (lobbyPage > 0) contents[LOBBY_PREVIOUS] = pageButton(false, "Lobby", lobbyPage, lobbyPages);
		if (lobbyPage < lobbyPages - 1) contents[LOBBY_NEXT] = pageButton(true, "Lobby", lobbyPage, lobbyPages);

		int sessionPages = totalPages(sessions.size());
		if (sessionPage > 0) contents[SESSION_PREVIOUS] = pageButton(false, "Active Games", sessionPage, sessionPages);
		if (sessionPage < sessionPages - 1) contents[SESSION_NEXT] = pageButton(true, "Active Games", sessionPage, sessionPages);

		if (lobbies.isEmpty()) contents[13] = placeholder("No open lobbies");
		if (sessions.isEmpty()) contents[22] = placeholder("No active games");

		inv.setContents(contents);
	}

	private void fillPage(ItemStack[] contents, List<Session> sessions, int page, int firstSlot,
			boolean isLobby, HashMap<Integer, Session> slots) {
		int start = page * PAGE_SIZE;
		for (int i = 0; i < PAGE_SIZE && start + i < sessions.size(); i++) {
			int slot = firstSlot + i;
			Session session = sessions.get(start + i);
			contents[slot] = buildSessionHead(session, isLobby);
			slots.put(slot, session);
		}
	}

	private int clampPage(int page, int entryCount) {
		return Math.min(page, totalPages(entryCount) - 1);
	}

	private int totalPages(int entryCount) {
		return Math.max(1, (entryCount + PAGE_SIZE - 1) / PAGE_SIZE);
	}

	private ItemStack pageButton(boolean next, String category, int page, int totalPages) {
		String direction = next ? "Next" : "Previous";
		String head = next ? ArtifactsInventory.NEXT_HEAD : ArtifactsInventory.PREV_HEAD;
		ItemStack item = CoreInventory.createButton(head,
				Component.text(direction + " " + category + " Page", NamedTextColor.YELLOW));
		ItemMeta meta = item.getItemMeta();
		meta.lore(List.of(Component.text("Page " + (page + 1) + " / " + totalPages, NamedTextColor.GRAY)));
		item.setItemMeta(meta);
		return item;
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
		lore.add(Component.text("Region: " + session.getRegion().getType().getDisplay(), NamedTextColor.GRAY));
		lore.add(Component.text("Nodes visited: " + session.getNodesVisited(), NamedTextColor.GRAY));
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
		lore.add(Component.empty());
		lore.add(Component.text("Players (" + session.getParty().size() + ")", NamedTextColor.GRAY));
		List<PlayerSessionData> players = new ArrayList<>(session.getParty().values());
		players.sort((first, second) -> {
			boolean firstIsHost = first.getUniqueId().equals(hostUuid);
			boolean secondIsHost = second.getUniqueId().equals(hostUuid);
			if (firstIsHost != secondIsHost) return firstIsHost ? -1 : 1;
			return first.getData().getDisplay().compareToIgnoreCase(second.getData().getDisplay());
		});
		for (PlayerSessionData player : players) {
			lore.add(Component.text("- " + player.getData().getDisplay(), NamedTextColor.WHITE));
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
		if (slot == LOBBY_PREVIOUS && lobbyPage > 0) {
			lobbyPage--;
			setupInventory();
			return;
		}
		if (slot == LOBBY_NEXT) {
			lobbyPage++;
			setupInventory();
			return;
		}
		if (slot == SESSION_PREVIOUS && sessionPage > 0) {
			sessionPage--;
			setupInventory();
			return;
		}
		if (slot == SESSION_NEXT) {
			sessionPage++;
			setupInventory();
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
