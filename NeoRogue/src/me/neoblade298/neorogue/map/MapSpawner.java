package me.neoblade298.neorogue.map;

import org.bukkit.configuration.ConfigurationSection;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;

public class MapSpawner {
	private MythicMob mob;
	private int amount, radius;
	private Coordinates coords;
	
	public MapSpawner(ConfigurationSection cfg, MapPiece piece) {
		mob = MythicBukkit.inst().getMobManager().getMythicMob(cfg.getString("mob")).get();
		amount = cfg.getInt("amount", 1);
		radius = cfg.getInt("radius", 0);
		String[] parsed = cfg.getString("coords").split(",");
		coords = new Coordinates(Integer.parseInt(parsed[0]), Integer.parseInt(parsed[1]),
				Integer.parseInt(parsed[2]), piece.getShape().getBaseLength() * 16 - 1, piece.getShape().getBaseHeight() * 16 - 1);
	}

	public MythicMob getMob() {
		return mob;
	}

	public int getAmount() {
		return amount;
	}

	public int getRadius() {
		return radius;
	}
	
	public Coordinates getCoordinates() {
		return coords;
	}
	
	public MapSpawnerInstance instantiate(MapPieceInstance settings, int xOff, int zOff) {
		return new MapSpawnerInstance(this, settings, xOff, zOff);
	}
}