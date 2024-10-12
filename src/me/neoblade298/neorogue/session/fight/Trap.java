package me.neoblade298.neorogue.session.fight;

import java.util.UUID;

import org.bukkit.Location;

public class Trap {
	private UUID uuid;
	private Location loc;

	public Trap(Location loc) {
		this.uuid = UUID.randomUUID();
		this.loc = loc;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public Location getLocation() {
		return loc;
	}
}
