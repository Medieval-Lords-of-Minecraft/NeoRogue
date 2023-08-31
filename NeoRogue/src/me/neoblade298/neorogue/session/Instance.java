package me.neoblade298.neorogue.session;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.event.player.PlayerInteractEvent;

import me.neoblade298.neorogue.player.PlayerSessionData;

public interface Instance {
	public void start(Session s);
	public void cleanup();
	public void handleInteractEvent(PlayerInteractEvent e);
	public String serialize(HashMap<UUID, PlayerSessionData> party);
}
