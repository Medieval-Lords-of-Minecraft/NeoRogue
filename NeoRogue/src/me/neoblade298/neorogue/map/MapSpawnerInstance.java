package me.neoblade298.neorogue.map;

import org.bukkit.Location;
import org.bukkit.Material;

import io.lumine.mythic.api.mobs.MythicMob;

public class MapSpawnerInstance {
	private MythicMob mob;
	private int amount, radius;
	private Location loc;
	
	public MapSpawnerInstance(MapSpawner original, MapPieceInstance inst) {
		this.mob = original.getMob();
		this.amount = original.getAmount();
		this.radius = original.getRadius();
		this.loc = original.getCoordinates().clone().applySettings(inst).toLocation();
		System.out.println("Spawner : " + inst.getPiece().getId() + " " + loc);
		
		this.loc.add(MapPieceInstance.X_FIGHT_OFFSET + inst.getX() * 16,
				MapPieceInstance.Y_OFFSET + inst.getY(),
				MapPieceInstance.Z_FIGHT_OFFSET + inst.getZ() * 16);
		this.loc.setX(-this.loc.getX());
		loc.getBlock().setType(Material.BLUE_WOOL);
		System.out.println("Final location : " + loc);
	}
}
