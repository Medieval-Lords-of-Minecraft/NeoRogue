package me.neoblade298.neorogue.session;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import me.neoblade298.neorogue.player.PlayerSessionInventory;
import me.neoblade298.neorogue.session.instance.*;

public class SessionManager implements Listener {
	private static HashMap<UUID, Session> sessions = new HashMap<UUID, Session>();
	
	public static Session createSession(Player p) {
		Session s = new Session(p);
		sessions.put(p.getUniqueId(), s);
		return s;
	}
	
	public static Session getSession(Player p) {
		return sessions.get(p.getUniqueId());
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
			
			if (s.getInstance() instanceof NodeSelectInstance ||
					s.getInstance() instanceof RestInstance) {
				e.setCancelled(true);
				p.setItemOnCursor(null);
				new PlayerSessionInventory(s.getData(uuid));
			}
		}
	}
	
	
	
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		if (e.getEntityType() != EntityType.PLAYER && e.getDamager().getType() != EntityType.PLAYER) return;
		
		boolean playerDamager = e.getEntityType() != EntityType.PLAYER;
		if (!sessions.containsKey(p.getUniqueId())) return;
		Session s = sessions.get(p.getUniqueId());
		
		if (!(s.getInstance() instanceof FightInstance)) return;
		((FightInstance) s.getInstance()).handleOnDamage(e, playerDamager);
	}
}
