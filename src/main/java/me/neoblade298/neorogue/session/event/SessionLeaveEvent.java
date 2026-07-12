package me.neoblade298.neorogue.session.event;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.neoblade298.neorogue.session.Session;

/**
 * Fired after a player has been removed from a NeoRogue session (leaving, being kicked,
 * disconnecting, or the session ending). Purely informational; the player is already out of the
 * session when this fires. The session may be null if it could not be resolved.
 */
public class SessionLeaveEvent extends Event {
	private static final HandlerList HANDLERS = new HandlerList();

	private final UUID uuid;
	private final Session session;

	public SessionLeaveEvent(UUID uuid, Session session) {
		this.uuid = uuid;
		this.session = session;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	// May be null if the session could not be resolved when the player was removed.
	public Session getSession() {
		return session;
	}

	// May be null if the player is offline (e.g. they left by disconnecting).
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
