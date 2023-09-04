package me.neoblade298.neorogue.session;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.scheduler.BukkitRunnable;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobDespawnEvent;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.map.MapSpawnerInstance;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.PlayerSessionInventory;
import me.neoblade298.neorogue.player.SessionSnapshot;
import me.neoblade298.neorogue.session.fights.*;

public class SessionManager implements Listener {
	private static HashMap<UUID, Session> sessions = new HashMap<UUID, Session>();
	private static HashMap<Plot, Session> sessionPlots = new HashMap<Plot, Session>();

	public static Session createSession(Player p, String name, int saveSlot) {
		Plot plot = findPlot();
		Session s = new Session(p, plot, name, saveSlot);
		sessions.put(p.getUniqueId(), s);
		sessionPlots.put(plot, s);
		
		Util.msg(p, "&7Successfully created a lobby!");
		return s;
	}
	
	public static Session loadSession(Player p, int saveSlot) {
		PlayerData pd = PlayerManager.getPlayerData(p.getUniqueId());
		SessionSnapshot ss = pd.getSnapshot(saveSlot);
		for (Entry<UUID, String> ent : ss.getPartyIds().entrySet()) {
			if (Bukkit.getPlayer(ent.getKey()) == null) {
				Util.displayError(p, "&cCannot load this save as &e" + ent.getValue() + " &cis not online!");
				return null;
			}
		}
		
		Plot plot = findPlot();
		Session s = new Session(p, plot, saveSlot);
		sessions.put(p.getUniqueId(), s);
		sessionPlots.put(plot, s);
		return s;
	}
	
	private static Plot findPlot() {
		for (int x = 0; x < 6; x++) {
			for (int z = 0; z < 100; z++) {
				Plot plot = new Plot(x, z);
				if (!sessionPlots.containsKey(plot)) {
					return plot;
				}
			}
		}
		
		return null;
	}

	public static void addToSession(UUID uuid, Session s) {
		sessions.put(uuid, s);
	}

	public static void removeFromSession(UUID uuid) {
		sessions.remove(uuid);
		Player p = Bukkit.getPlayer(uuid);
		if (p != null) p.teleport(NeoRogue.spawn);
	}

	public static void removeSession(Session s) {
		for (UUID uuid : s.getParty().keySet()) {
			removeFromSession(uuid);
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

		// If the inventory type is normal player inventory, open up a player session inventory
		openInventory(p, e);
	}

	private void openInventory(Player p, Cancellable e) {
		UUID uuid = p.getUniqueId();
		if (sessions.containsKey(uuid)) {
			Session s = sessions.get(uuid);
			e.setCancelled(true);

			if (s.getInstance() instanceof EditInventoryInstance) {
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
		if (!sessions.containsKey(uuid)) return;
		Session s = sessions.get(uuid);
		s.getInstance().handleInteractEvent(e);
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
		Entity ent = e.getEntity();
		UUID uuid = ent.getUniqueId();
		FightInstance.putFightData(uuid, new FightData((Damageable) ent, (MapSpawnerInstance) null));
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
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if (sessions.containsKey(p.getUniqueId())) {
			Session s = sessions.get(p.getUniqueId());
			s.teleportToInstance(p);
		}
		else {
			p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
		}
	}

	private void handleLeave(Player p) {
		if (sessions.containsKey(p.getUniqueId())) {
			Session s = sessions.get(p.getUniqueId());

			if (s.getInstance() instanceof LobbyInstance) {
				LobbyInstance li = (LobbyInstance) s.getInstance();
				li.leavePlayer(p);
			}
			else {
				if (s.getOnlinePlayers().size() <= 1) {
					endSession(s);
				}
			}
		}
	}
	
	public static void endSession(Session s) {
		s.cleanup();
		removeSession(s);
		new BukkitRunnable() {
			public void run() {
				try (Connection con = NeoCore.getConnection("NeoRogue-SessionManager");
						Statement insert = con.createStatement();
						Statement delete = con.createStatement()) {
					s.save(insert, delete);
				}
				catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}
}
