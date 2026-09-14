package me.neoblade298.neorogue.leaderboard;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import me.neoblade298.neocore.bukkit.leaderboard.LeaderboardService;
import me.neoblade298.neocore.bukkit.leaderboard.LeaderboardService.Display;
import me.neoblade298.neocore.bukkit.leaderboard.LeaderboardService.Options;
import me.neoblade298.neocore.bukkit.leaderboard.LeaderboardService.Renderer;
import me.neoblade298.neocore.bukkit.leaderboard.LeaderboardService.SqlRequest;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.leaderboard.LeaderboardConfig.LeaderboardLocation;
import me.neoblade298.neorogue.leaderboard.LeaderboardConfig.LeaderboardPeriod;
import me.neoblade298.neorogue.leaderboard.LeaderboardConfig.LeaderboardRunMode;
import me.neoblade298.neorogue.leaderboard.LeaderboardConfig.LeaderboardType;
import me.neoblade298.neorogue.player.SessionSnapshot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class LeaderboardManager {
	private static final long DEFAULT_REFRESH_SECONDS = 60L * 60L;
	private static final int DEFAULT_ENTRY_LIMIT = 10;
	private static final int MIN_WINRATE_RUNS = 10;

	private static final Map<String, LeaderboardLocation> locations = new LinkedHashMap<>();
	private static LeaderboardConfig config;
	private static LeaderboardService<BoardKey, LeaderboardRow> service;

	private LeaderboardManager() {
	}

	public static void init() {
		config = new LeaderboardConfig();
		service = new LeaderboardService<>(NeoRogue.inst(), loadOptions(), LeaderboardManager::query,
				new LeaderboardRenderer());
		service.start(LeaderboardManager::loadDisplays);
	}

	public static void reload() {
		if (service == null) return;
		service.configure(loadOptions());
		service.reload();
	}

	public static void cleanup() {
		if (service == null) return;
		service.close();
		service = null;
		locations.clear();
	}

	public static List<LeaderboardLocation> getLocations() {
		return List.copyOf(locations.values());
	}

	public static LeaderboardLocation add(LeaderboardType type, LeaderboardPeriod period, Integer notoriety,
			LeaderboardRunMode runMode, Location location) {
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
		if (service != null) service.refresh();
	}

	private static List<Display<BoardKey>> loadDisplays() {
		locations.clear();
		List<Display<BoardKey>> displays = new ArrayList<>();
		for (LeaderboardLocation leaderboard : config.load()) {
			Location location = leaderboard.toLocation();
			if (location == null) continue;
			locations.put(leaderboard.id(), leaderboard);
			displays.add(new Display<>(leaderboard.id(), BoardKey.from(leaderboard), location,
					leaderboard.entryLimit(), leaderboard.refreshSeconds() == null
							? null : leaderboard.refreshSeconds() * 20L));
		}
		return displays;
	}

	private static Options loadOptions() {
		long refreshSeconds = NeoRogue.inst().getConfig().getLong("leaderboards.refresh-seconds",
				DEFAULT_REFRESH_SECONDS);
		int entryLimit = NeoRogue.inst().getConfig().getInt("leaderboards.entries", DEFAULT_ENTRY_LIMIT);
		if (refreshSeconds <= 0) {
			NeoRogue.inst().getLogger().warning("leaderboards.refresh-seconds must be positive; using "
					+ DEFAULT_REFRESH_SECONDS);
			refreshSeconds = DEFAULT_REFRESH_SECONDS;
		}
		if (entryLimit <= 0) {
			NeoRogue.inst().getLogger().warning("leaderboards.entries must be positive; using "
					+ DEFAULT_ENTRY_LIMIT);
			entryLimit = DEFAULT_ENTRY_LIMIT;
		}
		return new Options("main", refreshSeconds * 20L, entryLimit, loadExcludedPlayerIds());
	}

	private static Set<UUID> loadExcludedPlayerIds() {
		HashSet<UUID> playerIds = new HashSet<>();
		for (String configuredId : NeoRogue.inst().getConfig().getStringList("leaderboards.excluded-player-uuids")) {
			try {
				playerIds.add(UUID.fromString(configuredId));
			} catch (IllegalArgumentException ex) {
				NeoRogue.inst().getLogger().warning("Invalid UUID in leaderboards.excluded-player-uuids: " + configuredId);
			}
		}
		return Set.copyOf(playerIds);
	}

	private static SqlRequest<LeaderboardRow> query(BoardKey key, int entryLimit, Set<UUID> excludedPlayerIds) {
		String aggregate = key.type() == LeaderboardType.WINRATE
				? "COUNT(*) AS runs, SUM(r.won) AS wins, AVG(r.won) AS score"
				: "MIN(r.playtime) AS score";
		StringBuilder sql = new StringBuilder("SELECT rp.playerUuid, ").append(aggregate)
				.append(" FROM neorogue_analytics_runs r")
				.append(" JOIN neorogue_analytics_run_players rp ON rp.runId = r.runId")
				.append(" WHERE r.endless = 0");
		List<Object> parameters = new ArrayList<>();
		if (!excludedPlayerIds.isEmpty()) {
			sql.append(" AND rp.playerUuid NOT IN (")
					.append(String.join(", ", Collections.nCopies(excludedPlayerIds.size(), "?")))
					.append(")");
			parameters.addAll(excludedPlayerIds.stream().map(UUID::toString).toList());
		}
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
		sql.append(" LIMIT ?");
		parameters.add(entryLimit);

		return new SqlRequest<>("NeoRogue", sql.toString(), parameters, result ->
				new LeaderboardRow(UUID.fromString(result.getString("playerUuid")), result.getDouble("score"),
						key.type() == LeaderboardType.WINRATE ? result.getInt("wins") : 0,
						key.type() == LeaderboardType.WINRATE ? result.getInt("runs") : 0));
	}

	private static long monthStart() {
		return ZonedDateTime.now(ZoneId.systemDefault()).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS)
				.toInstant().toEpochMilli();
	}

	private static Component render(BoardKey key, List<LeaderboardRow> rows) {
		Component text = title(key);
		if (rows.isEmpty()) {
			return text.append(Component.newline()).append(Component.text("No qualifying runs", NamedTextColor.GRAY));
		}
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

	private static final class LeaderboardRenderer implements Renderer<BoardKey, LeaderboardRow> {
		@Override
		public Component loading(BoardKey key) {
			return title(key).append(Component.newline()).append(Component.text("Loading...", NamedTextColor.GRAY));
		}

		@Override
		public Component unavailable(BoardKey key) {
			return title(key).append(Component.newline()).append(Component.text("Unavailable", NamedTextColor.RED));
		}

		@Override
		public Component render(BoardKey key, List<LeaderboardRow> rows) {
			return LeaderboardManager.render(key, rows);
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
}