package me.neoblade298.neorogue.map;

import org.bukkit.configuration.ConfigurationSection;

import me.neoblade298.neocore.shared.io.LineConfig;
import me.neoblade298.neocore.shared.io.LineConfigParser;

public class MapSpawner implements LineConfigParser<MapSpawner> {
	private MythicMob mob;
	private int amount, radius;
	private RotatableCoordinates coords;
	
	public MapSpawner(ConfigurationSection cfg) {
		this.mob = mob;
		this.amount = amount;
		this.radius = radius;
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

	@Override
	public MapSpawner create(LineConfig arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getKey() {
		// TODO Auto-generated method stub
		return null;
	}
}
