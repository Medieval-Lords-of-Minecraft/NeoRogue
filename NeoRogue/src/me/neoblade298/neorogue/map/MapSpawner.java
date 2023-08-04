package me.neoblade298.neorogue.map;

import org.bukkit.configuration.ConfigurationSection;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;

public class MapSpawner {
	private MythicMob mob;
	private int amount, radius;
	private RotatableCoordinates coords;
	
	public MapSpawner(ConfigurationSection cfg, MapPiece piece) {
		mob = MythicBukkit.inst().getMobManager().getMythicMob(cfg.getString("mob")).get();
		amount = cfg.getInt("amount", 1);
		radius = cfg.getInt("radius", 0);
		String[] parsed = cfg.getString("coords").split(",");
		coords = new RotatableCoordinates(Integer.parseInt(parsed[0]), Integer.parseInt(parsed[1]),
				Integer.parseInt(parsed[2]), piece);
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
	
	public RotatableCoordinates getCoordinates() {
		return coords;
	}
	
	public void rotate(int amount) {
		coords.rotate(amount);
	}
	public void flip(boolean xAxis) {
		coords.flip(xAxis);
	}
}
