package me.neoblade298.neorogue.map;

import java.util.Optional;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import me.neoblade298.neocore.shared.io.Section;
import me.neoblade298.neorogue.session.fight.Mob;

public class MapSpawner {
	private String id;
	private Mob mob;
	private MythicMob mythicMob;
	private Coordinates coords;
	private double radius;
	private int maxMobs;
	
	public MapSpawner(Section cfg, MapPiece piece) {
		id = cfg.getString("mob");
		mob = Mob.get(id);
		Optional<MythicMob> opt = MythicBukkit.inst().getMobManager().getMythicMob(id);
		mythicMob = opt.isPresent() ? opt.get() : null;
		coords = new Coordinates(piece, cfg.getString("coords"), true);
		radius = cfg.getDouble("radius");
		maxMobs = cfg.getInt("maxmobs", -1);
	}

	public MythicMob getMythicMob() {
		return mythicMob;
	}
	
	public Mob getMob() {
		return mob;
	}
	
	public Coordinates getCoordinates() {
		return coords;
	}
	
	public MapSpawnerInstance instantiate(MapPieceInstance settings, int xOff, int zOff) {
		return new MapSpawnerInstance(this, settings, xOff, zOff);
	}
	
	public double getRadius() {
		return radius;
	}
	
	public int getMaxMobs() {
		return maxMobs;
	}
}
