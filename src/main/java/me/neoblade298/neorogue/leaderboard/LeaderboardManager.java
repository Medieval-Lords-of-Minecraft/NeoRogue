package me.neoblade298.neorogue.leaderboard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scheduler.BukkitTask;

import me.neoblade298.neocore.shared.io.SQLManager;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.leaderboard.LeaderboardConfig.LeaderboardLocation;
import me.neoblade298.neorogue.leaderboard.LeaderboardConfig.LeaderboardPeriod;
import me.neoblade298.neorogue.leaderboard.LeaderboardConfig.LeaderboardRunMode;
import me.neoblade298.neorogue.leaderboard.LeaderboardConfig.LeaderboardType;
import me.neoblade298.neorogue.player.SessionSnapshot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class LeaderboardManager implements Listener {
	private static final long REFRESH_TICKS = 60L * 60L * 20L;
	private static final String ENTITY_TAG = "neorogue_leaderboard";
	private static final int LIMIT = 10;
	private static final int MIN_WINRATE_RUNS = 10;

	private static final Map<String, ActiveDisplay> displays = new LinkedHashMap<>();
	private static LeaderboardConfig config;
	private static BukkitTask refreshTask;
	private static int generation;

	private LeaderboardManager() {
	}

	public static void init() {
		config = new LeaderboardConfig();
		Bukkit.getPluginManager().registerEvents(new LeaderboardManager(), NeoRogue.inst());
		refreshTask = Bukkit.getScheduler().runTaskTimer(NeoRogue.inst(), LeaderboardManager::refresh,
				REFRESH_TICKS, REFRESH_TICKS);
		Bukkit.getScheduler().runTaskLater(NeoRogue.inst(), LeaderboardManager::refresh, 200L);
	}

	@EventHandler
	public void onWorldLoad(WorldLoadEvent event) {
		reload();
	}

	@EventHandler
	public void onServerLoad(ServerLoadEvent event) {
		// Plugin enable order is not a reliable indication that every configured world has
		// finished loading. Rebuild once startup is complete, including worlds whose load
		// event happened before this listener was registered.
		Bukkit.getScheduler().runTask(NeoRogue.inst(), LeaderboardManager::reload);
	}

	public static void reload() {
		if (config == null) return;
		generation++;
		List<LeaderboardLocation> locations = config.load();
		Map<String, Location> bukkitLocations = new LinkedHashMap<>();
		for (LeaderboardLocation location : locations) {
			Location bukkitLocation = location.toLocation();
			if (bukkitLocation == null) continue;
			// getEntities() ensures previously persisted displays in this otherwise-unloaded
			// chunk are available to the tagged-entity cleanup below.
			bukkitLocation.getChunk().getEntities();
			bukkitLocations.put(location.id(), bukkitLocation);
		}
		removeDisplays();
		removeTaggedDisplays();
		for (LeaderboardLocation location : locations) {
			Location bukkitLocation = bukkitLocations.get(location.id());
			if (bukkitLocation == null) continue;
			TextDisplay display = NeoRogue.createHologram(bukkitLocation, loadingText(location), Billboard.FIXED);
			display.setRotation(location.yaw(), location.pitch());
			// Leaderboard chunks are not held open. The display must be saved so it returns
			// when a player later loads the chunk.
			display.setPersistent(true);
			display.addScoreboardTag(ENTITY_TAG);
			displays.put(location.id(), new ActiveDisplay(location, display));
		}
		refresh();
	}

	public static void cleanup() {
		generation++;
		if (refreshTask != null) {
			refreshTask.cancel();
			refreshTask = null;
		}
		removeDisplays();
	}

	public static List<LeaderboardLocation> getLocations() {
		return displays.values().stream().map(ActiveDisplay::location).toList();
	}

	public static LeaderboardLocation add(LeaderboardType type, LeaderboardPeriod period, Integer notoriety,
			LeaderboardRunMode runMode, org.bukkit.Location location) {
		LeaderboardLocation added = config.add(type, period, notoriety, runMode, location);
		reload();
		return added;
	}

	public static boolean remove(String id) {
		if (!config.remove(id)) return false;
		reload();
		return true;
	}

	public static void refresh() {
		if (displays.isEmpty()) return;
		int refreshGeneration = ++generation;
		Map<BoardKey, List<String>> locationsByBoard = new HashMap<>();
		for (ActiveDisplay active : displays.values()) {
			locationsByBoard.computeIfAbsent(BoardKey.from(active.location()), ignored -> new ArrayList<>())
					.add(active.location().id());
		}
		Bukkit.getScheduler().runTaskAsynchronously(NeoRogue.inst(), () -> {
			Map<BoardKey, List<LeaderboardRow>> results = new HashMap<>();
			for (BoardKey key : locationsByBoard.keySet()) {
				try {
					results.put(key, query(key));
				} catch (SQLException ex) {
					NeoRogue.inst().getLogger().log(java.util.logging.Level.SEVERE,
							"Failed to refresh " + key.type().display() + " leaderboard", ex);
					results.put(key, null);
				}
			}
			Bukkit.getScheduler().runTask(NeoRogue.inst(), () -> applyResults(refreshGeneration, locationsByBoard, results));
		});
	}

	private static List<LeaderboardRow> query(BoardKey key) throws SQLException {
		String aggregate = key.type() == LeaderboardType.WINRATE
				? "COUNT(*) AS runs, SUM(r.won) AS wins, AVG(r.won) AS score"
				: "MIN(r.playtime) AS score";
		StringBuilder sql = new StringBuilder("SELECT rp.playerUuid, ").append(aggregate)
				.append(" FROM neorogue_analytics_runs r")
				.append(" JOIN neorogue_analytics_run_players rp ON rp.runId = r.runId")
				.append(" WHERE r.endless = 0");
		List<Object> parameters = new ArrayList<>();
		if (key.type() == LeaderboardType.FASTEST_CLEAR) sql.append(" AND r.won = 1");
		if (key.period() == LeaderboardPeriod.MONTHLY) {
			sql.append(" AND r.ts >= ?");
			parameters.add(monthStart());
		}
		if (key.notoriety() != null) {
			sql.append(" AND r.notoriety = ?");
			parameters.add(key.notoriety());
		}
		if (key.runMode() != LeaderboardRunMode.ALL) {
			sql.append(" AND r.competitive = ?");
			parameters.add(key.runMode() == LeaderboardRunMode.COMPETITIVE ? 1 : 0);
		}
		sql.append(" GROUP BY rp.playerUuid");
		if (key.type() == LeaderboardType.WINRATE) sql.append(" HAVING COUNT(*) >= ").append(MIN_WINRATE_RUNS);
		sql.append(key.type() == LeaderboardType.WINRATE
				? " ORDER BY score DESC, wins DESC, runs DESC"
				: " ORDER BY score ASC");
		sql.append(" LIMIT ").append(LIMIT);

		List<LeaderboardRow> rows = new ArrayList<>();
		try (Connection connection = SQLManager.getConnection("NeoRogue");
				PreparedStatement statement = connection.prepareStatement(sql.toString())) {
			for (int i = 0; i < parameters.size(); i++) statement.setObject(i + 1, parameters.get(i));
			try (ResultSet result = statement.executeQuery()) {
				while (result.next()) {
					rows.add(new LeaderboardRow(UUID.fromString(result.getString("playerUuid")),
							result.getDouble("score"),
							key.type() == LeaderboardType.WINRATE ? result.getInt("wins") : 0,
							key.type() == LeaderboardType.WINRATE ? result.getInt("runs") : 0));
				}
			}
		}
		return rows;
	}

	private static long monthStart() {
		return ZonedDateTime.now(ZoneId.systemDefault()).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS)
				.toInstant().toEpochMilli();
	}

	private static void applyResults(int refreshGeneration, Map<BoardKey, List<String>> locationsByBoard,
			Map<BoardKey, List<LeaderboardRow>> results) {
		if (refreshGeneration != generation) return;
		for (Map.Entry<BoardKey, List<String>> entry : locationsByBoard.entrySet()) {
			List<LeaderboardRow> rows = results.get(entry.getKey());
			Component text = rows == null ? unavailableText(entry.getKey()) : render(entry.getKey(), rows);
			for (String locationId : entry.getValue()) {
				ActiveDisplay active = displays.get(locationId);
				TextDisplay display = resolveDisplay(active);
				if (display != null) display.text(NeoRogue.withTextDisplayShadow(text));
			}
		}
	}

	private static TextDisplay resolveDisplay(ActiveDisplay active) {
		if (active == null) return null;
		if (active.display().isValid()) return active.display();
		Location location = active.location().toLocation();
		if (location == null) return null;
		location.getChunk().getEntities();
		Entity entity = Bukkit.getEntity(active.display().getUniqueId());
		if (!(entity instanceof TextDisplay display) || !display.isValid()) return null;
		displays.put(active.location().id(), new ActiveDisplay(active.location(), display));
		return display;
	}

	private static Component render(BoardKey key, List<LeaderboardRow> rows) {
		Component text = title(key);
		if (rows.isEmpty()) return text.append(Component.newline()).append(Component.text("No qualifying runs", NamedTextColor.GRAY));
		for (int i = 0; i < rows.size(); i++) {
			LeaderboardRow row = rows.get(i);
			OfflinePlayer player = Bukkit.getOfflinePlayer(row.playerId());
			String name = player.getName() != null ? player.getName() : row.playerId().toString().substring(0, 8);
			String value = key.type() == LeaderboardType.WINRATE
					? String.format(Locale.US, "%.1f%% (%d/%d)", row.score() * 100, row.wins(), row.runs())
					: SessionSnapshot.formatPlaytime((long) row.score());
			text = text.append(Component.newline())
					.append(Component.text((i + 1) + ". ", NamedTextColor.GOLD))
					.append(Component.text(name, NamedTextColor.WHITE))
					.append(Component.text(" - " + value, NamedTextColor.YELLOW));
		}
		return text;
	}

	private static Component loadingText(LeaderboardLocation location) {
		return title(BoardKey.from(location)).append(Component.newline()).append(Component.text("Loading...", NamedTextColor.GRAY));
	}

	private static Component unavailableText(BoardKey key) {
		return title(key).append(Component.newline()).append(Component.text("Unavailable", NamedTextColor.RED));
	}

	private static Component title(BoardKey key) {
		String period = key.period() == LeaderboardPeriod.MONTHLY ? "Monthly" : "All-Time";
		String notoriety = key.notoriety() == null ? "All Notoriety" : "Notoriety " + key.notoriety();
		String mode = switch (key.runMode()) {
		case ALL -> "All Runs";
		case COMPETITIVE -> "Competitive";
		case CASUAL -> "Casual";
		};
		return Component.text(key.type().display(), NamedTextColor.AQUA)
				.append(Component.newline())
				.append(Component.text(period + " | " + notoriety + " | " + mode, NamedTextColor.GRAY));
	}

	private static void removeDisplays() {
		for (ActiveDisplay active : displays.values()) {
			if (active.display().isValid()) {
				active.display().remove();
			}
		}
		displays.clear();
	}

	private static void removeTaggedDisplays() {
		for (org.bukkit.World world : Bukkit.getWorlds()) {
			for (TextDisplay display : world.getEntitiesByClass(TextDisplay.class)) {
				if (!display.getScoreboardTags().contains(ENTITY_TAG)) continue;
				display.remove();
			}
		}
	}

	private record BoardKey(LeaderboardType type, LeaderboardPeriod period, Integer notoriety,
			LeaderboardRunMode runMode) {
		private static BoardKey from(LeaderboardLocation location) {
			return new BoardKey(location.type(), location.period(), location.notoriety(), location.runMode());
		}
	}

	private record LeaderboardRow(UUID playerId, double score, int wins, int runs) {
	}

	private record ActiveDisplay(LeaderboardLocation location, TextDisplay display) {
	}
}