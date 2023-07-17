package me.neoblade298.neorogue.session;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.PlayerSessionInventory;
import me.neoblade298.neorogue.session.instance.*;

public class SessionManager implements Listener {
	private static HashMap<UUID, Session> sessions = new HashMap<UUID, Session>();

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent e) {
		Player p = (Player) e.getPlayer();
		UUID uuid = p.getUniqueId();
		if (sessions.containsKey(uuid) {
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
