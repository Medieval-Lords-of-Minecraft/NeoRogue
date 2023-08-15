package me.neoblade298.neorogue.session;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Tag;
import org.bukkit.block.Sign;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobDespawnEvent;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.player.PlayerClass;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.PlayerSessionInventory;
import me.neoblade298.neorogue.session.fights.*;

public class SessionManager implements Listener {
	private static HashMap<UUID, Session> sessions = new HashMap<UUID, Session>();
	private static HashMap<Plot, Session> sessionPlots = new HashMap<Plot, Session>();

	public static Session createSession(Player p, String name) {
		// Find an available plot
		Plot plot = null;
		boolean found = false;

		for (int x = 0; x < 6; x++) {
			for (int z = 0; z < 100; z++) {
				plot = new Plot(x, z);
				if (!sessionPlots.containsKey(plot)) {
					found = true;
					break;
				}
			}
			if (found) {
				break;
			}
		}

		// Create session on plot
		Session s = new Session(p, plot, name);
		sessions.put(p.getUniqueId(), s);
		sessionPlots.put(plot, s);
		return s;
	}

	public static void addToSession(UUID uuid, Session s) {
		sessions.put(uuid, s);
	}

	public static void removeFromSession(UUID uuid) {
		sessions.remove(uuid);
	}

	public static void removeSession(Session s) {
		for (UUID uuid : s.getParty().keySet()) {
			sessions.remove(uuid);
		}
		sessionPlots.remove(s.getPlot());
	}

	public static Session getSession(Plot p) {
		return sessionPlots.get(p);
	}

	public static Session getSession(UUID uuid) {
		return sessions.get(uuid);
	}

	public static Session getSession(Player p) {
		return sessions.get(p.getUniqueId());
	}

	public static Collection<Session> getSessions() {
		return sessionPlots.values();
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent e) {
		handlePlayerInventoryInteract(e);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		handlePlayerInventoryInteract(e);
	}

	@EventHandler
	public void onSwap(PlayerSwapHandItemsEvent e) {
		Player p = e.getPlayer();
		openInventory(p, e);
	}

	private void handlePlayerInventoryInteract(InventoryInteractEvent e) {
		Player p = (Player) e.getWhoClicked();

		if (e.getView().getTopInventory().getType() != InventoryType.CRAFTING) return;

		// If the inventory type is crafting, open up a player session inventory
		openInventory(p, e);
	}

	private void openInventory(Player p, Cancellable e) {
		UUID uuid = p.getUniqueId();
		if (sessions.containsKey(uuid)) {
			Session s = sessions.get(uuid);
			e.setCancelled(true);

			if (s.getInstance() instanceof NodeSelectInstance || s.getInstance() instanceof RestInstance) {
				p.setItemOnCursor(null);
				new PlayerSessionInventory(s.getData(uuid));
			}
		}
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		if (e.getEntityType() != EntityType.PLAYER && e.getDamager().getType() != EntityType.PLAYER) return;

		boolean playerDamager = e.getEntityType() != EntityType.PLAYER;
		UUID uuid = playerDamager ? e.getDamager().getUniqueId() : e.getEntity().getUniqueId();
		if (!sessions.containsKey(uuid)) return;
		Session s = sessions.get(uuid);

		if (!(s.getInstance() instanceof FightInstance)) return;
		FightInstance.handleDamage(e, playerDamager);
	}

	@EventHandler
	public void onHotbarSwap(PlayerItemHeldEvent e) {
		UUID uuid = e.getPlayer().getUniqueId();
		if (!sessions.containsKey(uuid)) return;
		Session s = sessions.get(uuid);

		if (!(s.getInstance() instanceof FightInstance)) return;
		FightInstance.handleHotbarSwap(e);
	}

	@EventHandler(ignoreCancelled = false)
	public void onInteract(PlayerInteractEvent e) {
		UUID uuid = e.getPlayer().getUniqueId();
		Action a = e.getAction();
		if (!sessions.containsKey(uuid)) return;
		Session s = sessions.get(uuid);
		
		// Node select
		if (s.getInstance() instanceof NodeSelectInstance) {
			if (a != Action.RIGHT_CLICK_BLOCK || !Tag.BUTTONS.isTagged(e.getClickedBlock().getType())) return;
			
			// Validation
			for (Entry<UUID, PlayerSessionData> ent : s.getParty().entrySet()) {
				Player member = ent.getValue().getPlayer();
				if (member == null) {
					for (Player online : s.getOnlinePlayers()) {
						Util.displayError(online, "&cAt least one party member (&4" + ent.getValue().getData().getDisplay() + "&c) is not online!");
					}
					return;
				}
				
				if (!ent.getValue().saveStorage()) {
					for (Player online : s.getOnlinePlayers()) {
						Util.displayError(online, "&&4" + ent.getValue().getData().getDisplay() + "&c has too many items in their inventory! They must drop some "
								+ "to satisfy their storage limit of &e" + ent.getValue().getMaxStorage() + "&c!");
					}
					return;
				}
			}
			s.getArea().getNodeFromLocation(e.getClickedBlock().getLocation()).startInstance(s);
			return;
		}
			
		// Class select
		if (s.getInstance() instanceof LobbyInstance) {
			if (a != Action.RIGHT_CLICK_BLOCK || !Tag.SIGNS.isTagged(e.getClickedBlock().getType())) return;
			LobbyInstance li = (LobbyInstance) s.getInstance();
			
			Sign sign = (Sign) e.getClickedBlock().getState();
			char c = ChatColor.stripColor(sign.getLine(2)).charAt(0);
			
			switch (c) {
			case 'S': li.switchClass(uuid, PlayerClass.SWORDSMAN);
			break;
			case 'T': li.switchClass(uuid, PlayerClass.THIEF);
			break;
			case 'A': li.switchClass(uuid, PlayerClass.ARCHER);
			break;
			case 'M': li.switchClass(uuid, PlayerClass.MAGE);
			break;
			}
		}

		// Check fight instance
		if (!(s.getInstance() instanceof FightInstance)) return;
		
		if (a == Action.LEFT_CLICK_AIR || a == Action.LEFT_CLICK_BLOCK) {
			FightInstance.handleLeftClick(e);
		}
		// Right click
		else if (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
			FightInstance.handleRightClick(e);
		}
	}

	@EventHandler
	public void onMythicDespawn(MythicMobDespawnEvent e) {
		FightInstance.handleMythicDespawn(e);
	}

	@EventHandler
	public void onMythicDeath(MythicMobDeathEvent e) {
		FightInstance.handleMythicDeath(e);
	}

	@EventHandler
	public void onMythicSpawn(MythicMobSpawnEvent e) {
		UUID uuid = e.getEntity().getUniqueId();
		FightInstance.putFightData(uuid, new FightData((Damageable) e.getEntity()));
	}

	@EventHandler
	public void onHungerChange(FoodLevelChangeEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void onHungerRegen(EntityRegainHealthEvent e) {
		if (e.getRegainReason() == RegainReason.SATIATED) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onKick(PlayerKickEvent e) {
		handleLeave(e.getPlayer());
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		handleLeave(e.getPlayer());
	}

	private void handleLeave(Player p) {
		if (sessions.containsKey(p.getUniqueId())) {
			Session s = sessions.get(p.getUniqueId());

			if (s.getInstance() instanceof LobbyInstance) {
				LobbyInstance li = (LobbyInstance) s.getInstance();
				li.leavePlayer(p);
			}
		}
	}
}
