package me.neoblade298.neorogue.session;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.RegisteredListener;

import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.PlayerSessionInventory;
import me.neoblade298.neorogue.session.instance.*;

public class SessionManager implements Listener {
	private static HashMap<UUID, Session> sessions = new HashMap<UUID, Session>();

	@EventHandler(ignoreCancelled = false)
	public void onInventoryInteract(InventoryInteractEvent e) {
		handlePlayerInventoryInteract(e);
	}
	
	@EventHandler(ignoreCancelled = false)
	public void onInventoryClick(InventoryClickEvent e) {
		handlePlayerInventoryInteract(e);
	}
	
	private void handlePlayerInventoryInteract(InventoryInteractEvent e) {
		Player p = (Player) e.getWhoClicked();
		
		if (e.getView().getTopInventory().getType() != InventoryType.CRAFTING) return;
		
		// Strictly for testing
		e.setCancelled(true);
		new PlayerSessionInventory(new PlayerSessionData(p.getUniqueId()));
		
		// If the inventory type is crafting, open up a player session inventory
		UUID uuid = p.getUniqueId();
		if (sessions.containsKey(uuid)) {
			e.setCancelled(true);
			Session s = sessions.get(uuid);
			
			if (s.getInstance() instanceof NodeSelectInstance) {
				new PlayerSessionInventory(s.getData(uuid));
			}
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		
	}
}
