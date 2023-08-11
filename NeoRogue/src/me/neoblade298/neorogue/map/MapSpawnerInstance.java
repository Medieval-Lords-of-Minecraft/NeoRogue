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
		System.out.println("Location: " + original.getCoordinates());
		System.out.println("Location post: " + original.getCoordinates().clone().applySettings(inst));
		this.loc = original.getCoordinates().clone().applySettings(inst).toLocation();
		System.out.println(MapPieceInstance.X_FIGHT_OFFSET + " " + xOff +" " + inst.getX() * 16 + " " + loc.getX() + ": ");
		
		this.loc.add(MapPieceInstance.X_FIGHT_OFFSET + xOff + inst.getX() * 16,
				MapPieceInstance.Y_OFFSET,
				MapPieceInstance.Z_FIGHT_OFFSET + zOff + inst.getZ() * 16);
		this.loc.setX(-this.loc.getX());
		System.out.println(loc.getBlockX());
		loc.getBlock().setType(Material.BLUE_WOOL);
		System.out.println("Instantiated spawner at " + loc);
	}
}
