package me.neoblade298.neorogue.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.listeners.InventoryListener;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.SpectateSelectInventory;
import me.neoblade298.neorogue.session.event.GenerateShopEvent;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class ShopInstance extends EditInventoryInstance {
	private static final double SPAWN_X = Session.SHOP_X + 5.5, SPAWN_Z = Session.SHOP_Z + 2.5,
			HOLO_X = 0, HOLO_Y = 1, HOLO_Z = 4;
	static final int NUM_ITEMS = 10;
	
	private HashMap<UUID, ShopContents> shops = new HashMap<UUID, ShopContents>();
	private HashSet<UUID> ready = new HashSet<UUID>();
	private TextDisplay holo;
	private boolean allReady = false;
	
	public ShopInstance(Session s) {
		super(s, SPAWN_X, SPAWN_Z);
		spectatorLines = playerLines;
	}
	
	public ShopInstance(Session s, HashMap<UUID, PlayerSessionData> party) {
		this(s);
		for (Entry<UUID, PlayerSessionData> ent : party.entrySet()) {
			shops.put(ent.getKey(), ShopContents.deserializeShopItems(ent.getValue().getInstanceData()));
		}
	}

	@Override
	public void setup() {
		for (PlayerSessionData data : s.getParty().values()) {
			Player p = data.getPlayer();
			teleportRandomly(p);
			GenerateShopEvent ev = new GenerateShopEvent();
			data.trigger(SessionTrigger.GENERATE_SHOP, ev);
			shops.put(p.getUniqueId(), new ShopContents(s, data, ev.getDiscountMultiplier()));
		}
		for (UUID uuid : s.getSpectators().keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			teleportRandomly(p);
		}
		super.setup();

		// Setup hologram
		Component text = Component.text("Open the chest, then click the").appendNewline().append(Component.text("stone button", NamedTextColor.GOLD).decoration(TextDecoration.BOLD, true))
				.append(Component.text(" when you're ready!"));
		holo = NeoRogue.createHologram(spawn.clone().add(HOLO_X, HOLO_Y, HOLO_Z), text);
	}

	@Override
	public void updateBoardLines() {
		playerLines.clear();
		playerLines.add(createBoardLine(s.getParty().get(s.getHost()), true));

		ArrayList<PlayerSessionData> sorted = new ArrayList<PlayerSessionData>();
		for (PlayerSessionData data : s.getParty().values()) {
			if (s.getHost() == data.getUniqueId()) continue;
			sorted.add(data);
		}
		Collections.sort(sorted);
		for (PlayerSessionData data : sorted) {
			playerLines.add(createBoardLine(data, false));
		}
	}

	private String createBoardLine(PlayerSessionData data, boolean isHost) {
		UUID uuid = data.getUniqueId();
		String line = ready.contains(uuid) ? "§a✓ §f" : "§c✗ §f";
		if (isHost) {
			line += "★ ";
		}
		line += data.getData().getDisplay() + "§7 - §e" + data.getCoins() + " coins";
		return line;
	}

	@Override
	public void cleanup(boolean pluginDisable) {
		super.cleanup(pluginDisable);
		holo.remove();
	}

	@Override
	public void handleSpectatorInteractEvent(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		e.setCancelled(true);
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.CHEST) {
			new SpectateSelectInventory(s, p, null, true);
		}
		else {
			super.handleSpectatorInteractEvent(e);
		}
	}

	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		if (e.getHand() != EquipmentSlot.HAND)
			return;

		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.CHEST) {
			e.setCancelled(true);
			
			if (ready.contains(uuid)) {
				Util.displayError(p, "You've already been marked as ready! Press the button again to un-ready!");
				return;
			}

			p.playSound(p, Sound.BLOCK_ENDER_CHEST_OPEN, 1F, 1F);
			new ShopInventory(s.getParty().get(uuid), shops.get(uuid));
		}

		else if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.STONE_BUTTON) {
			handleReady(p);
		}

		else {
			// Have to do this because apparently adventure mode does right and left click when you open a chest
			// This is a workaround to prevent the player from interacting with the map while the inventory is open
			if (InventoryListener.hasOpenCoreInventory(p)) return;
			super.handleInteractEvent(e);
		}
	}
	
	public void spectateShop(Player spectator, UUID uuid) {
		new ShopInventory(s.getParty().get(uuid), shops.get(uuid), spectator);
	}
	
	public void handleReady(Player p) {
		UUID uuid = p.getUniqueId();
		if (s.isBusy()) return;

		NodeSelectInstance next = new NodeSelectInstance(s);


		if (allReady) {
			if (!s.canSetInstance(next)) {
				return;
			}
			handleNextInstance(next);
			return;
		}

		if (!ready.contains(uuid)) {
			ready.add(uuid);
			s.broadcast("<yellow>" + p.getName() + " <gray>is <green>ready</green>!");
			
			// Last person to get ready (or everyone was already ready), check if instance can be set first
			if (ready.size() >= s.getParty().size()) {
				allReady = true;
				if (!s.canSetInstance(next)) {
					return;
				}
				handleNextInstance(next);
			}
		}
		else {
			s.broadcast("<yellow>" + p.getName() + " <gray>is no longer ready!");
			ready.remove(uuid);
		}
		updateBoardLines();
	}

	private void handleNextInstance(NodeSelectInstance next) {
		s.broadcast("Everyone is ready! Teleporting back to node select...");
		s.broadcastSound(Sound.ENTITY_PLAYER_LEVELUP);
		s.setBusy(true);
		new BukkitRunnable() {
			public void run() {
				s.setInstance(next);
				s.setBusy(false);
			}
		}.runTaskLater(NeoRogue.inst(), 60L);
	}

	@Override
	public String serialize(HashMap<UUID, PlayerSessionData> party) {
		for (Entry<UUID, ShopContents> ent : shops.entrySet()) {
			PlayerSessionData data = party.get(ent.getKey());
			String ser = ent.getValue().serialize();
			data.setInstanceData(ser);
		}
		return "SHOP:";
	}

	@Override
	public void handlePlayerLeaveParty(OfflinePlayer p) {
		ready.remove(p.getUniqueId());
		if (ready.size() == s.getParty().size()) {
			s.broadcast("Everyone is ready! Teleporting back to node select...");
			s.broadcastSound(Sound.ENTITY_PLAYER_LEVELUP);
			s.setBusy(true);
			new BukkitRunnable() {
				public void run() {
					s.setInstance(new NodeSelectInstance(s));
					s.setBusy(false);
				}
			}.runTaskLater(NeoRogue.inst(), 60L);
		}
	}
}
