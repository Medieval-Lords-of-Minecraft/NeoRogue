package me.neoblade298.neorogue.map;

import org.bukkit.Location;
import org.bukkit.Material;

import io.lumine.mythic.api.mobs.MythicMob;

public class MapSpawnerInstance {
	private MythicMob mob;
	private int amount, radius;
	private Location loc;
	
	public MapSpawnerInstance(MapSpawner original, MapPieceInstance inst, int xOff, int zOff) {
		this.mob = original.getMob();
		this.amount = original.getAmount();
		this.radius = original.getRadius();
		this.loc = original.getCoordinates().clone().applySettings(inst).toLocation();
		
		this.loc.add(MapPieceInstance.X_FIGHT_OFFSET + xOff,
				MapPieceInstance.Y_OFFSET,
				MapPieceInstance.Z_FIGHT_OFFSET + zOff);
		this.loc.setX(-this.loc.getX());
		loc.getBlock().setType(Material.BLUE_WOOL);
	}
}
