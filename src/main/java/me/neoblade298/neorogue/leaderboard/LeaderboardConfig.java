package me.neoblade298.neorogue.leaderboard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import me.neoblade298.neorogue.NeoRogue;

public class LeaderboardConfig {
	private static final String LOCATIONS = "locations";

	private final File file;
	private YamlConfiguration yml;

	public LeaderboardConfig() {
		file = new File(NeoRogue.inst().getDataFolder(), "leaderboards.yml");
	}

	public List<LeaderboardLocation> load() {
		yml = YamlConfiguration.loadConfiguration(file);
		List<LeaderboardLocation> locations = new ArrayList<>();
		ConfigurationSection section = yml.getConfigurationSection(LOCATIONS);
		if (section == null) return locations;

		for (String id : section.getKeys(false)) {
			ConfigurationSection locationSection = section.getConfigurationSection(id);
			if (locationSection == null) continue;
			try {
				LeaderboardType type = LeaderboardType.valueOf(locationSection.getString("leaderboard", "").toUpperCase(Locale.ROOT));
				LeaderboardPeriod period = LeaderboardPeriod.valueOf(locationSection.getString("period", "ALLTIME").toUpperCase(Locale.ROOT));
				LeaderboardRunMode runMode = LeaderboardRunMode.valueOf(locationSection.getString("run-mode", "ALL").toUpperCase(Locale.ROOT));
				Integer notoriety = locationSection.isInt("notoriety") ? locationSection.getInt("notoriety") : null;
				if (notoriety != null && (notoriety < 0 || notoriety > 10)) throw new IllegalArgumentException();
				String worldName = locationSection.getString("world");
				if (worldName == null || Bukkit.getWorld(worldName) == null) {
					NeoRogue.inst().getLogger().warning("Leaderboard location '" + id + "' references unloaded world '" + worldName + "'; it will be retried when the world loads");
					continue;
				}
				locations.add(new LeaderboardLocation(id, type, period, notoriety, runMode, worldName,
						locationSection.getDouble("x"), locationSection.getDouble("y"), locationSection.getDouble("z"),
						(float) locationSection.getDouble("yaw"), (float) locationSection.getDouble("pitch")));
			} catch (IllegalArgumentException ex) {
				NeoRogue.inst().getLogger().warning("Leaderboard location '" + id + "' has an invalid option, skipping");
			}
		}
		return locations;
	}

	public LeaderboardLocation add(LeaderboardType type, LeaderboardPeriod period, Integer notoriety,
			LeaderboardRunMode runMode, Location location) {
		String id = UUID.randomUUID().toString().substring(0, 8);
		LeaderboardLocation entry = new LeaderboardLocation(id, type, period, notoriety, runMode,
				location.getWorld().getName(), location.getX(), location.getY(), location.getZ(),
				location.getYaw(), location.getPitch());
		String path = LOCATIONS + "." + id + ".";
		yml.set(path + "leaderboard", type.name());
		yml.set(path + "period", period.name());
		yml.set(path + "notoriety", notoriety);
		yml.set(path + "run-mode", runMode.name());
		yml.set(path + "world", entry.world());
		yml.set(path + "x", entry.x());
		yml.set(path + "y", entry.y());
		yml.set(path + "z", entry.z());
		yml.set(path + "yaw", entry.yaw());
		yml.set(path + "pitch", entry.pitch());
		save();
		return entry;
	}

	public boolean remove(String id) {
		if (!yml.contains(LOCATIONS + "." + id)) return false;
		yml.set(LOCATIONS + "." + id, null);
		save();
		return true;
	}

	private void save() {
		try {
			yml.save(file);
		} catch (IOException ex) {
			throw new IllegalStateException("Could not save leaderboards.yml", ex);
		}
	}

	public enum LeaderboardType {
		WINRATE("Highest Win Rate"),
		FASTEST_CLEAR("Fastest Clear");

		private final String display;

		LeaderboardType(String display) {
			this.display = display;
		}

		public String display() {
			return display;
		}
	}

	public enum LeaderboardPeriod {
		MONTHLY, ALLTIME
	}

	public enum LeaderboardRunMode {
		ALL, COMPETITIVE, CASUAL
	}

	public record LeaderboardLocation(String id, LeaderboardType type, LeaderboardPeriod period,
			Integer notoriety, LeaderboardRunMode runMode, String world, double x, double y, double z,
			float yaw, float pitch) {
		public Location toLocation() {
			World loadedWorld = Bukkit.getWorld(world);
			return loadedWorld == null ? null : new Location(loadedWorld, x, y, z, yaw, pitch);
		}
	}
}