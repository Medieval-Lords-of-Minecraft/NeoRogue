package me.neoblade298.neorogue.session.event;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.neoblade298.neorogue.session.Session;

/**
 * Fired after a player has been added to a NeoRogue session (lobby join, party join, or
 * spectating). Purely informational; the player is already in the session when this fires.
 */
public class SessionJoinEvent extends Event {
	private static final HandlerList HANDLERS = new HandlerList();

	private final UUID uuid;
	private final Session session;

	public SessionJoinEvent(UUID uuid, Session session) {
		this.uuid = uuid;
		this.session = session;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public Session getSession() {
		return session;
	}

	// May be null if the player is offline.
	public Player getPlayer() {
		return Bukkit.getPlayer(uuid);
	}

	public OfflinePlayer getOfflinePlayer() {
		return Bukkit.getOfflinePlayer(uuid);
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
