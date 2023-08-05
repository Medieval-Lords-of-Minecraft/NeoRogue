package me.neoblade298.neorogue.map;

import org.bukkit.Location;

import io.lumine.mythic.api.mobs.MythicMob;

public class MapSpawnerInstance {
	private MythicMob mob;
	private int amount, radius;
	private Location loc;
	
	public MapSpawnerInstance(MapSpawner original, MapPieceInstance inst) {
		this.mob = original.getMob();
		this.amount = original.getAmount();
		this.radius = original.getRadius();
		this.loc = original.getCoordinates().applySettings(inst).toLocation();
	}
}
