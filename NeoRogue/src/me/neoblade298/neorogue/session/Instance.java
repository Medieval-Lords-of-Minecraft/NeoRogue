package me.neoblade298.neorogue.session;

import org.bukkit.event.player.PlayerInteractEvent;

public interface Instance {
	public void start(Session s);
	public void cleanup();
	public void handleInteractEvent(PlayerInteractEvent e);
}
