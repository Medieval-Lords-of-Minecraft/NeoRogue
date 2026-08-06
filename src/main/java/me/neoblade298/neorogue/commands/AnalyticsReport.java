package me.neoblade298.neorogue.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.io.SQLManager;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.map.MapPiece;
import me.neoblade298.neorogue.region.NodeType;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.fight.Mob;
import me.neoblade298.neorogue.session.fight.Mob.MobType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

// Runs and prints aggregated effectiveness analytics from the per-fight fact tables. Invoked by the
// /nrlytics subcommands, which handle argument parsing; each method here just reports parsed args.
public class AnalyticsReport {
	private static final DecimalFormat df = new DecimalFormat("#.##");
	private static final int MIN_SAMPLES = 10;
	private static final int LEADERBOARD_LIMIT = 10;
	private static final String LOW_SAMPLE_MARKER = " <red>!</red>";
	public static final List<String> EQUIPMENT_METRIC_KEYS = List.of(
			"DAMAGE", "BUFF", "MITIGATED", "SHIELDS", "HEALING", "STATUS", "WINRATE");

	public enum EquipmentMetric {
		DAMAGE("damage", "fe.damageDealt", "Damage", "Highest damage", "Lowest damage", ""),
		BUFF("buff", "fe.damageBuffAdded", "Damage Buff", "Highest damage added", "Lowest damage added", ""),
		MITIGATED("mitigated", "fe.damageMitigated", "Mitigation", "Highest mitigation", "Lowest mitigation", ""),
		SHIELDS("shields", "fe.shieldsApplied", "Shields", "Highest shields applied", "Lowest shields applied", ""),
		HEALING("healing", "fe.healingDone", "Healing", "Highest healing", "Lowest healing", ""),
		STATUS("status", "fe.statusTotal", "Status", "Highest status stacks", "Lowest status stacks", ""),
		WINRATE("winrate", "fe.outcome", "Winrate", "Highest winrate", "Lowest winrate", "%");

		private final String key, column, display, highLabel, lowLabel, suffix;

		private EquipmentMetric(String key, String column, String display, String highLabel, String lowLabel, String suffix) {
			this.key = key;
			this.column = column;
			this.display = display;
			this.highLabel = highLabel;
			this.lowLabel = lowLabel;
			this.suffix = suffix;
		}

		public static EquipmentMetric fromKey(String key) {
			for (EquipmentMetric metric : values()) {
				if (metric.key.equalsIgnoreCase(key)) return metric;
			}
			return null;
		}
	}

	// Filterable columns exposed by the "equipment" view. Shared with the command layer so tab
	// completion and query building stay in sync. Columns are qualified (fe = neorogue_analytics_fight_equipment,
	// f = neorogue_analytics_fights) because the query joins the two. equipClass is comma-separated (FIND_IN_SET).
	public static final List<AnalyticsFilters.FilterOption> EQUIPMENT_FILTER_OPTIONS = List.of(
			new AnalyticsFilters.FilterOption("id", "UPPER(fe.equipmentId)", false, equipmentIds()),
			new AnalyticsFilters.FilterOption("class", "fe.equipClass", true, enumNames(EquipmentClass.values())),
			new AnalyticsFilters.FilterOption("rarity", "fe.rarity", false, enumNames(Rarity.values())),
			new AnalyticsFilters.FilterOption("type", "fe.equipType", false, enumNames(EquipmentType.values())),
			new AnalyticsFilters.FilterOption("fighttype", "f.nodeType", false,
					List.of(NodeType.FIGHT.name(), NodeType.MINIBOSS.name(), NodeType.BOSS.name())),
			new AnalyticsFilters.FilterOption("region", "f.regionType", false, regionTypes()),
			new AnalyticsFilters.FilterOption("level", "f.level", false, null),
			new AnalyticsFilters.FilterOption("regions", "f.regionsCompleted", false, null),
			new AnalyticsFilters.FilterOption("party", "f.partySize", false, null),
			new AnalyticsFilters.FilterOption("notoriety", "f.notoriety", false, null),
			new AnalyticsFilters.FilterOption("endless", "f.endless", false, List.of("0", "1")));

	public static final List<AnalyticsFilters.FilterOption> CLASS_FILTER_OPTIONS = List.of(
			new AnalyticsFilters.FilterOption("class", "p.playerClass", false, playerClasses()),
			new AnalyticsFilters.FilterOption("region", "r.regionType", false, regionTypes()),
			new AnalyticsFilters.FilterOption("level", "r.level", false, null),
			new AnalyticsFilters.FilterOption("regions", "r.regionsCompleted", false, null),
			new AnalyticsFilters.FilterOption("party", "r.partySize", false, null),
			new AnalyticsFilters.FilterOption("notoriety", "r.notoriety", false, null),
			new AnalyticsFilters.FilterOption("endless", "r.endless", false, List.of("0", "1")),
			new AnalyticsFilters.FilterOption("competitive", "r.competitive", false, List.of("0", "1")));

	public static final List<AnalyticsFilters.FilterOption> PICKRATE_FILTER_OPTIONS = List.of(
			new AnalyticsFilters.FilterOption("source", "o.source", false, offerSources()),
			new AnalyticsFilters.FilterOption("class", "o.equipClass", true, enumNames(EquipmentClass.values())),
			new AnalyticsFilters.FilterOption("rarity", "o.rarity", false, enumNames(Rarity.values())),
			new AnalyticsFilters.FilterOption("type", "o.equipType", false, enumNames(EquipmentType.values())),
			new AnalyticsFilters.FilterOption("region", "o.regionType", false, regionTypes()),
			new AnalyticsFilters.FilterOption("nodetype", "o.nodeType", false, enumNames(NodeType.values())),
			new AnalyticsFilters.FilterOption("level", "o.level", false, null),
			new AnalyticsFilters.FilterOption("upgraded", "o.upgraded", false, List.of("0", "1")),
			new AnalyticsFilters.FilterOption("notoriety", "r.notoriety", false, null),
			new AnalyticsFilters.FilterOption("party", "r.partySize", false, null),
			new AnalyticsFilters.FilterOption("regions", "r.regionsCompleted", false, null),
			new AnalyticsFilters.FilterOption("endless", "r.endless", false, List.of("0", "1")),
			new AnalyticsFilters.FilterOption("competitive", "r.competitive", false, List.of("0", "1")),
			new AnalyticsFilters.FilterOption("won", "r.won", false, List.of("0", "1")));

	public static final List<AnalyticsFilters.FilterOption> CHANCE_FILTER_OPTIONS = List.of(
			new AnalyticsFilters.FilterOption("set", "c.setId", false, null),
			new AnalyticsFilters.FilterOption("class", "c.playerClass", false, playerClasses()),
			new AnalyticsFilters.FilterOption("region", "c.regionType", false, regionTypes()),
			new AnalyticsFilters.FilterOption("nodetype", "c.nodeType", false, enumNames(NodeType.values())),
			new AnalyticsFilters.FilterOption("level", "c.level", false, null),
			new AnalyticsFilters.FilterOption("individual", "c.individual", false, List.of("0", "1")),
			new AnalyticsFilters.FilterOption("notoriety", "r.notoriety", false, null));

	public static final List<AnalyticsFilters.FilterOption> MOB_FILTER_OPTIONS = List.of(
			new AnalyticsFilters.FilterOption("class", "fm.playerClass", false, playerClasses()),
			new AnalyticsFilters.FilterOption("region", "f.regionType", false, regionTypes()),
			new AnalyticsFilters.FilterOption("fighttype", "f.nodeType", false, enumNames(NodeType.values())),
			new AnalyticsFilters.FilterOption("level", "f.level", false, null),
			new AnalyticsFilters.FilterOption("regions", "f.regionsCompleted", false, null),
			new AnalyticsFilters.FilterOption("party", "f.partySize", false, null),
			new AnalyticsFilters.FilterOption("notoriety", "f.notoriety", false, null),
			new AnalyticsFilters.FilterOption("endless", "f.endless", false, List.of("0", "1")));

	private static List<String> enumNames(Enum<?>[] values) {
		ArrayList<String> names = new ArrayList<String>();
		for (Enum<?> v : values) names.add(v.name());
		return names;
	}

	private static List<String> equipmentIds() {
		ArrayList<String> ids = new ArrayList<String>();
		for (String id : me.neoblade298.neorogue.equipment.Equipment.getEquipmentIds()) ids.add(id.toUpperCase());
		return ids;
	}

	private static List<String> playerClasses() {
		ArrayList<String> classes = new ArrayList<String>();
		for (EquipmentClass equipmentClass : EquipmentClass.values()) {
			if (equipmentClass != EquipmentClass.SHOP && equipmentClass != EquipmentClass.CLASSLESS) {
				classes.add(equipmentClass.name());
			}
		}
		return classes;
	}

	private static List<String> offerSources() {
		return List.of("SHOP", "REWARD");
	}

	private static List<String> regionTypes() {
		ArrayList<String> regions = new ArrayList<String>();
		for (RegionType regionType : RegionType.values()) {
			if (!regionType.name().contains("DEBUG")) regions.add(regionType.name());
		}
		return regions;
	}

	private static String lowSampleMarker(int samples) {
		return samples < MIN_SAMPLES ? LOW_SAMPLE_MARKER : "";
	}

	private static void addLowSampleDisclaimer(ArrayList<String> lines) {
		if (lines.stream().anyMatch(line -> line.contains(LOW_SAMPLE_MARKER))) {
			lines.add(0, "<red>!</red> <gray>Fewer than " + MIN_SAMPLES + " samples; interpret cautiously.");
		}
	}

	private static void addReportMeta(ArrayList<String> lines, AnalyticsFilters filters) {
		if (lines.isEmpty() && !filters.hasErrors()) return;
		lines.add(0, "<gray>Page " + filters.getPage() + " | low samples "
				+ (filters.filterLowSamples() ? "hidden" : "shown"));
		for (int i = filters.getErrors().size() - 1; i >= 0; i--) {
			lines.add(0, "<red>" + filters.getErrors().get(i));
		}
		if (!filters.filterLowSamples()) addLowSampleDisclaimer(lines);
	}

	private static String pageClause(AnalyticsFilters filters) {
		return " LIMIT " + (LEADERBOARD_LIMIT + 1) + " OFFSET " + filters.getOffset(LEADERBOARD_LIMIT);
	}

	private static boolean trimPage(List<?> rows) {
		boolean hasNext = rows.size() > LEADERBOARD_LIMIT;
		while (rows.size() > LEADERBOARD_LIMIT) rows.remove(rows.size() - 1);
		return hasNext;
	}

	private static void sendPageControls(CommandSender sender, String baseCommand, AnalyticsFilters filters) {
		if (filters.getPage() <= 1 && !filters.hasNextPage()) return;
		Component controls = Component.empty();
		if (filters.getPage() > 1) {
			controls = controls.append(Component.text("[Previous]", NamedTextColor.YELLOW)
					.clickEvent(ClickEvent.runCommand(filters.pageCommand(baseCommand, filters.getPage() - 1)))
					.hoverEvent(HoverEvent.showText(Component.text("Go to page " + (filters.getPage() - 1)))));
		}
		if (filters.getPage() > 1 && filters.hasNextPage()) controls = controls.append(Component.space());
		if (filters.hasNextPage()) {
			controls = controls.append(Component.text("[Next]", NamedTextColor.YELLOW)
					.clickEvent(ClickEvent.runCommand(filters.pageCommand(baseCommand, filters.getPage() + 1)))
					.hoverEvent(HoverEvent.showText(Component.text("Go to page " + (filters.getPage() + 1)))));
		}
		Util.msgRaw(sender, controls);
	}

	private AnalyticsReport() {}

	// Class-wide run winrates. A sample is one player/class participating in one completed run;
	// DISTINCT prevents fights and individual mobs from multiplying that run's contribution.
	public static void classWinrates(CommandSender s, int version, AnalyticsFilters filters) {
		new BukkitRunnable() {
			@Override
			public void run() {
				ArrayList<String> lines = new ArrayList<String>();
				try (Connection con = SQLManager.getConnection("NeoRogue")) {
					StringBuilder sql = new StringBuilder("SELECT p.playerClass, COUNT(*) AS runs, SUM(r.won) AS wins,"
							+ " AVG(r.won) AS winrate FROM ("
							+ " SELECT rp.playerClass, rp.playerUuid, rp.runId FROM neorogue_analytics_run_players rp"
							+ " WHERE rp.balanceVersion = ?"
							+ " UNION ALL"
							+ " SELECT DISTINCT fm.playerClass, fm.playerUuid, f.runId"
							+ " FROM neorogue_analytics_fight_mobs fm"
							+ " JOIN neorogue_analytics_fights f ON f.fightId = fm.fightId"
							+ " WHERE fm.balanceVersion = ? AND f.runId <> ''"
							+ " AND NOT EXISTS (SELECT 1 FROM neorogue_analytics_run_players rp2 WHERE rp2.runId = f.runId)"
							+ ") p"
							+ " JOIN neorogue_analytics_runs r ON r.runId = p.runId"
							+ " WHERE r.balanceVersion = ?");
					filters.appendWhere(sql);
					sql.append(" GROUP BY p.playerClass");
					if (filters.filterLowSamples()) sql.append(" HAVING COUNT(*) >= ").append(MIN_SAMPLES);
					sql.append(" ORDER BY winrate DESC").append(pageClause(filters)).append(";");
					try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
						ps.setInt(1, version);
						ps.setInt(2, version);
						ps.setInt(3, version);
						filters.bind(ps, 4);
						try (ResultSet rs = ps.executeQuery()) {
							int row = 0;
							while (rs.next()) {
								if (++row > LEADERBOARD_LIMIT) {
									filters.setHasNextPage(true);
									break;
								}
								int runs = rs.getInt("runs");
								lines.add("  <yellow>" + df.format(100.0 * rs.getDouble("winrate"))
										+ "%</yellow> <white>" + rs.getString("playerClass") + "</white> <gray>| "
										+ rs.getInt("wins") + "/" + runs + lowSampleMarker(runs));
							}
						}
					}
					addReportMeta(lines, filters);
				}
				catch (SQLException ex) {
					lines.clear();
					lines.add("<red>Failed to query analytics (see console).");
					ex.printStackTrace();
				}

				new BukkitRunnable() {
					@Override
					public void run() {
						Util.msgRaw(s, "<gold>=== Class Winrates (balance v" + version + ") ===");
						if (lines.isEmpty()) {
							Util.msgRaw(s, "<yellow>No class run outcomes recorded.");
							return;
						}
						for (String line : lines) Util.msgRaw(s, line);
						sendPageControls(s, "/nrlytics classes", filters);
					}
				}.runTask(NeoRogue.inst());
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}

	// Single equipment id: effectiveness, statuses applied, and pickrate by source.
	public static void equipment(CommandSender s, String equipmentId, int version) {
		final String id = equipmentId.endsWith("+") ? equipmentId.substring(0, equipmentId.length() - 1) : equipmentId;
		new BukkitRunnable() {
			@Override
			public void run() {
				ArrayList<String> lines = new ArrayList<String>();
				try (Connection con = SQLManager.getConnection("NeoRogue")) {
					queryEquipment(con, id, version, lines);
					queryStatuses(con, id, version, lines);
					queryPickrate(con, id, version, lines);
					addLowSampleDisclaimer(lines);
				}
				catch (SQLException ex) {
					lines.clear();
					lines.add("<red>Failed to query analytics (see console).");
					ex.printStackTrace();
				}

				new BukkitRunnable() {
					@Override
					public void run() {
						Util.msgRaw(s, "<gold>=== Analytics: <yellow>" + id + "</yellow> (balance v" + version + ") ===");
						if (lines.isEmpty()) {
							Util.msgRaw(s, "<yellow>No recorded contributions for this equipment.");
							return;
						}
						for (String line : lines) {
							Util.msgRaw(s, line);
						}
					}
				}.runTask(NeoRogue.inst());
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}

	private static void queryEquipment(Connection con, String id, int version, ArrayList<String> lines) throws SQLException {
		String sql = "SELECT upgraded, COUNT(*) AS n, SUM(outcome) AS wins,"
				+ " AVG(damageDealt) AS dmg, AVG(damageBuffAdded) AS buff, AVG(damageMitigated) AS mit,"
				+ " AVG(shieldsApplied) AS shields, AVG(healingDone) AS heal, AVG(statusTotal) AS status"
				+ " FROM neorogue_analytics_fight_equipment WHERE equipmentId = ? AND balanceVersion = ? GROUP BY upgraded;";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, id);
			ps.setInt(2, version);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					boolean upgraded = rs.getInt("upgraded") == 1;
					int n = rs.getInt("n");
					int wins = rs.getInt("wins");
					double winrate = n > 0 ? (100.0 * wins / n) : 0;
					lines.add("<aqua>" + (upgraded ? "Upgraded" : "Base") + "</aqua> <gray>(" + n + " fights, "
							+ wins + " wins)" + lowSampleMarker(n));
					lines.add("  <white>Winrate:</white> <yellow>" + df.format(winrate) + "%");
					lines.add("  <white>Avg Damage:</white> " + df.format(rs.getDouble("dmg"))
							+ " <gray>| Buff:</gray> " + df.format(rs.getDouble("buff"))
							+ " <gray>| Mitigated:</gray> " + df.format(rs.getDouble("mit")));
					lines.add("  <white>Avg Shields:</white> " + df.format(rs.getDouble("shields"))
							+ " <gray>| Healing:</gray> " + df.format(rs.getDouble("heal"))
							+ " <gray>| Status:</gray> " + df.format(rs.getDouble("status")));
				}
			}
		}
	}

	private static void queryStatuses(Connection con, String id, int version, ArrayList<String> lines) throws SQLException {
		String sql = "SELECT upgraded, statusType, COUNT(*) AS n, SUM(outcome) AS wins, AVG(stacks) AS avgStacks"
				+ " FROM neorogue_analytics_fight_equipment_status WHERE equipmentId = ? AND balanceVersion = ?"
				+ " GROUP BY upgraded, statusType ORDER BY upgraded, statusType;";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, id);
			ps.setInt(2, version);
			try (ResultSet rs = ps.executeQuery()) {
				boolean header = false;
				while (rs.next()) {
					if (!header) {
						lines.add("<gold>Statuses applied:");
						header = true;
					}
					boolean upgraded = rs.getInt("upgraded") == 1;
					int n = rs.getInt("n");
					int wins = rs.getInt("wins");
					double winrate = n > 0 ? (100.0 * wins / n) : 0;
					lines.add("  <aqua>" + (upgraded ? "+" : " ") + "</aqua> <white>" + rs.getString("statusType")
							+ ":</white> <yellow>" + df.format(rs.getDouble("avgStacks")) + "</yellow> avg stacks"
							+ " <gray>(" + df.format(winrate) + "% wr, " + n + ")" + lowSampleMarker(n));
				}
			}
		}
	}

	private static void queryPickrate(Connection con, String id, int version, ArrayList<String> lines) throws SQLException {
		String sql = "SELECT source, upgraded, COUNT(*) AS offered, SUM(picked) AS picked"
				+ " FROM neorogue_analytics_equipment_offers WHERE equipmentId = ? AND balanceVersion = ?"
				+ " GROUP BY source, upgraded ORDER BY source, upgraded;";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, id);
			ps.setInt(2, version);
			try (ResultSet rs = ps.executeQuery()) {
				boolean header = false;
				while (rs.next()) {
					if (!header) {
						lines.add("<gold>Pickrate:");
						header = true;
					}
					String source = rs.getString("source");
					boolean upgraded = rs.getInt("upgraded") == 1;
					int offered = rs.getInt("offered");
					int picked = rs.getInt("picked");
					double rate = offered > 0 ? (100.0 * picked / offered) : 0;
					lines.add("  <aqua>" + source + (upgraded ? "+" : "") + ":</aqua> <yellow>" + df.format(rate)
							+ "%</yellow> <gray>(" + picked + "/" + offered + " offered)" + lowSampleMarker(offered));
				}
			}
		}
	}

	// View: equipment ranked by the selected contribution metric, filtered by any of the equipment
	// filter columns. Shows the highest and lowest entries, mirroring the mob leaderboard.
	public static void equipmentLeaderboard(CommandSender s, int version, EquipmentMetric metric,
			AnalyticsFilters filters) {
		new BukkitRunnable() {
			@Override
			public void run() {
				ArrayList<String> lines = new ArrayList<String>();
				try (Connection con = SQLManager.getConnection("NeoRogue")) {
					queryEquipmentLeaderboard(con, version, metric, filters, lines);
					addReportMeta(lines, filters);
				}
				catch (SQLException ex) {
					lines.clear();
					lines.add("<red>Failed to query analytics (see console).");
					ex.printStackTrace();
				}

				new BukkitRunnable() {
					@Override
					public void run() {
						Util.msgRaw(s, "<gold>=== Equipment " + metric.display + " (balance v" + version + ", " + filters.summary()
								+ ") ===");
						if (lines.isEmpty()) {
							Util.msgRaw(s, "<yellow>No equipment recorded.");
							return;
						}
						for (String line : lines) {
							Util.msgRaw(s, line);
						}
						sendPageControls(s, "/nrlytics equipment metric=" + metric.key, filters);
					}
				}.runTask(NeoRogue.inst());
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}

	private static void queryEquipmentLeaderboard(Connection con, int version, EquipmentMetric metric,
			AnalyticsFilters filters, ArrayList<String> lines) throws SQLException {
		// Join the fight facts so views can filter on fight-level columns (fight type, regions completed).
		// outcome/balanceVersion exist on both tables, so they're qualified to the equipment table.
		StringBuilder sql = new StringBuilder("SELECT fe.equipmentId AS equipmentId, fe.upgraded AS upgraded,"
				+ " COUNT(*) AS n, SUM(fe.outcome) AS wins, AVG(" + metric.column + ") AS metricValue"
				+ " FROM neorogue_analytics_fight_equipment fe JOIN neorogue_analytics_fights f ON fe.fightId = f.fightId"
				+ " WHERE fe.balanceVersion = ?");
		filters.appendWhere(sql);
		sql.append(" GROUP BY fe.equipmentId, fe.upgraded");
		if (filters.filterLowSamples()) sql.append(" HAVING COUNT(*) >= ").append(MIN_SAMPLES);

		ArrayList<String> top = new ArrayList<String>();
		try (PreparedStatement ps = con.prepareStatement(sql.toString() + " ORDER BY metricValue DESC" + pageClause(filters) + ";")) {
			int idx = 1;
			ps.setInt(idx++, version);
			filters.bind(ps, idx);
			collectEquipmentLeaderboardRows(ps, top, LEADERBOARD_LIMIT + 1, metric);
		}
		filters.setHasNextPage(trimPage(top));
		if (top.isEmpty()) return;

		lines.add("<red>" + metric.highLabel + ":");
		lines.addAll(top);

		ArrayList<String> bottom = new ArrayList<String>();
		try (PreparedStatement ps = con.prepareStatement(sql.toString() + " ORDER BY metricValue ASC" + pageClause(filters) + ";")) {
			int idx = 1;
			ps.setInt(idx++, version);
			filters.bind(ps, idx);
			collectEquipmentLeaderboardRows(ps, bottom, LEADERBOARD_LIMIT + 1, metric);
		}
		filters.setHasNextPage(filters.hasNextPage() | trimPage(bottom));
		lines.add("<green>" + metric.lowLabel + ":");
		lines.addAll(bottom);
	}

	private static void collectEquipmentLeaderboardRows(PreparedStatement ps, ArrayList<String> rows, int limit,
			EquipmentMetric metric) throws SQLException {
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next() && rows.size() < limit) {
				boolean upgraded = rs.getInt("upgraded") == 1;
				int n = rs.getInt("n");
				int wins = rs.getInt("wins");
				double value = rs.getDouble("metricValue");
				if (metric == EquipmentMetric.WINRATE) value *= 100;
				double winrate = n > 0 ? (100.0 * wins / n) : 0;
				String winrateContext = metric == EquipmentMetric.WINRATE ? "" : " | " + df.format(winrate) + "% WR";
				rows.add("  <yellow>" + df.format(value) + metric.suffix + "</yellow> <white>" + rs.getString("equipmentId")
						+ (upgraded ? "+" : "") + "</white> <gray>| " + n + "F" + winrateContext
						+ lowSampleMarker(n));
			}
		}
	}

	// Equipment pickrate leaderboard (optionally filtered to a single offer source: SHOP or REWARD).
	public static void pickrate(CommandSender s, int version, String source, String eqClass, String sortBy,
			AnalyticsFilters filters) {
		new BukkitRunnable() {
			@Override
			public void run() {
				ArrayList<String> lines = new ArrayList<String>();
				try (Connection con = SQLManager.getConnection("NeoRogue")) {
					queryLeaderboard(con, version, source, eqClass, sortBy, filters, lines);
					addReportMeta(lines, filters);
				}
				catch (SQLException ex) {
					lines.clear();
					lines.add("<red>Failed to query analytics (see console).");
					ex.printStackTrace();
				}

				new BukkitRunnable() {
					@Override
					public void run() {
						Util.msgRaw(s, "<gold>=== Pickrate Leaderboard (balance v" + version
								+ (source != null ? ", " + source : "")
								+ (eqClass != null ? ", " + eqClass : "")
								+ (sortBy != null ? ", sorted by " + sortBy : "") + ") ===");
						if (lines.isEmpty()) {
							Util.msgRaw(s, "<yellow>No offers recorded.");
							return;
						}
						for (String line : lines) {
							Util.msgRaw(s, line);
						}
						String baseCommand = "/nrlytics pickrate";
						if (source != null) baseCommand += " " + source;
						if (eqClass != null) baseCommand += " " + eqClass;
						if (eqClass != null && sortBy != null) baseCommand += " " + sortBy;
						sendPageControls(s, baseCommand, filters);
					}
				}.runTask(NeoRogue.inst());
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}

	private static void queryLeaderboard(Connection con, int version, String source, String eqClass, String sortBy,
			AnalyticsFilters filters, ArrayList<String> lines)
			throws SQLException {
		StringBuilder sql = new StringBuilder("SELECT o.equipmentId AS equipmentId, o.upgraded AS upgraded,"
				+ " COUNT(*) AS offered, SUM(o.picked) AS picked, (SUM(o.picked) / COUNT(*)) AS rate"
				+ " FROM neorogue_analytics_equipment_offers o"
				+ " LEFT JOIN neorogue_analytics_runs r ON r.runId = o.runId WHERE o.balanceVersion = ?");
		if (source != null) sql.append(" AND o.source = ?");
		if (eqClass != null) sql.append(" AND FIND_IN_SET(?, o.equipClass)");
		filters.appendWhere(sql);
		sql.append(" GROUP BY o.equipmentId, o.upgraded");
		if (filters.filterLowSamples()) sql.append(" HAVING COUNT(*) >= ").append(MIN_SAMPLES);

		String orderClause = (sortBy != null && sortBy.equalsIgnoreCase("class")) ? " ORDER BY equipmentId ASC" : " ORDER BY rate DESC";
		ArrayList<String[]> rows = new ArrayList<String[]>();
		try (PreparedStatement ps = con.prepareStatement(sql.toString() + orderClause + pageClause(filters) + ";")) {
			int idx = 1;
			ps.setInt(idx++, version);
			if (source != null) ps.setString(idx++, source);
			if (eqClass != null) ps.setString(idx++, eqClass);
			filters.bind(ps, idx);
			collectLeaderboardRows(ps, rows, LEADERBOARD_LIMIT + 1);
		}
		filters.setHasNextPage(trimPage(rows));
		if (rows.isEmpty()) return;

		if (sortBy != null && sortBy.equalsIgnoreCase("class")) {
			lines.add("<green>All equipment (sorted by class):");
			for (String[] row : rows) lines.add(row[0]);
		} else {
			lines.add("<green>Most picked:");
			for (String[] row : rows) lines.add(row[0]);

			rows.clear();
			try (PreparedStatement ps = con.prepareStatement(sql.toString() + " ORDER BY rate ASC" + pageClause(filters) + ";")) {
				int idx = 1;
				ps.setInt(idx++, version);
				if (source != null) ps.setString(idx++, source);
				if (eqClass != null) ps.setString(idx++, eqClass);
				filters.bind(ps, idx);
				collectLeaderboardRows(ps, rows, LEADERBOARD_LIMIT + 1);
			}
			filters.setHasNextPage(filters.hasNextPage() | trimPage(rows));
			lines.add("<red>Least picked:");
			for (String[] row : rows) lines.add(row[0]);
		}
	}

	private static void collectLeaderboardRows(PreparedStatement ps, ArrayList<String[]> rows, int limit) throws SQLException {
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next() && rows.size() < limit) {
				boolean upgraded = rs.getInt("upgraded") == 1;
				int offered = rs.getInt("offered");
				int picked = rs.getInt("picked");
				double rate = offered > 0 ? (100.0 * picked / offered) : 0;
				String line = "  <yellow>" + df.format(rate) + "%</yellow> <white>" + rs.getString("equipmentId")
						+ (upgraded ? "+" : "") + "</white> <gray>| " + picked + "/" + offered
						+ lowSampleMarker(offered);
				rows.add(new String[] { line });
			}
		}
	}

	// Leaderboard of chance-event option pick rate, computed as picked / valid so options are only
	// counted when they were actually selectable for the player.
	public static void chance(CommandSender s, int version, String setId, String playerClass,
			AnalyticsFilters filters) {
		new BukkitRunnable() {
			@Override
			public void run() {
				ArrayList<String> lines = new ArrayList<String>();
				try (Connection con = SQLManager.getConnection("NeoRogue")) {
					queryChanceLeaderboard(con, version, setId, playerClass, filters, lines);
					addReportMeta(lines, filters);
				}
				catch (SQLException ex) {
					lines.clear();
					lines.add("<red>Failed to query analytics (see console).");
					ex.printStackTrace();
				}

				new BukkitRunnable() {
					@Override
					public void run() {
						Util.msgRaw(s, "<gold>=== Chance Pickrate (balance v" + version
								+ (setId != null ? ", " + setId : "")
								+ (playerClass != null ? ", " + playerClass : "") + ") ===");
						if (lines.isEmpty()) {
							Util.msgRaw(s, "<yellow>No chance options recorded.");
							return;
						}
						for (String line : lines) {
							Util.msgRaw(s, line);
						}
						String baseCommand = "/nrlytics chance";
						if (setId != null) baseCommand += " " + setId;
						if (playerClass != null) baseCommand += " " + playerClass;
						sendPageControls(s, baseCommand, filters);
					}
				}.runTask(NeoRogue.inst());
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}

	// Leaderboard of mobs ranked by average damage dealt to the party per fight they appear in.
	public static void mobs(CommandSender s, int version, String regionType, String playerClass,
			AnalyticsFilters filters) {
		mobLeaderboard(s, version, regionType, playerClass, null, "Mob Damage Leaderboard",
				"/nrlytics mobs", filters);
	}

	// Same leaderboard restricted to the boss target mobs declared by BOSS map pieces.
	public static void bosses(CommandSender s, int version, String playerClass, AnalyticsFilters filters) {
		Set<String> ids = collectTargetMobIds(Map.getBossPieces(), MobType.BOSS);
		mobLeaderboard(s, version, null, playerClass, ids, "Boss Damage Leaderboard",
				"/nrlytics bosses", filters);
	}

	// Same leaderboard restricted to the miniboss target mobs declared by MINIBOSS map pieces.
	public static void minibosses(CommandSender s, int version, String playerClass, AnalyticsFilters filters) {
		Set<String> ids = collectTargetMobIds(Map.getMinibossPieces(), MobType.MINIBOSS);
		mobLeaderboard(s, version, null, playerClass, ids, "Miniboss Damage Leaderboard",
				"/nrlytics minibosses", filters);
	}

	// Unions the target mob ids of every map piece across all regions. Used to scope the mob
	// leaderboard to just the actual boss/miniboss entities (excluding adds spawned in those fights).
	private static Set<String> collectTargetMobIds(java.util.HashMap<RegionType, ArrayList<MapPiece>> pieces,
			MobType type) {
		Set<String> ids = new HashSet<String>(Mob.getStatIds(type));
		if (pieces == null) return ids;
		for (ArrayList<MapPiece> list : pieces.values()) {
			if (list == null) continue;
			for (MapPiece piece : list) {
				if (piece == null || piece.getTargets() == null) continue;
				for (String target : piece.getTargets()) ids.add(Mob.getStatId(target));
			}
		}
		return ids;
	}

	private static void mobLeaderboard(CommandSender s, int version, String regionType, String playerClass,
			Set<String> mobIdWhitelist, String title, String command, AnalyticsFilters filters) {
		new BukkitRunnable() {
			@Override
			public void run() {
				ArrayList<String> lines = new ArrayList<String>();
				// An empty (but non-null) whitelist means no boss/miniboss ids were found; nothing matches.
				if (mobIdWhitelist == null || !mobIdWhitelist.isEmpty()) {
					try (Connection con = SQLManager.getConnection("NeoRogue")) {
						queryMobLeaderboard(con, version, regionType, playerClass, mobIdWhitelist, filters, lines);
						addReportMeta(lines, filters);
					}
					catch (SQLException ex) {
						lines.clear();
						lines.add("<red>Failed to query analytics (see console).");
						ex.printStackTrace();
					}
				}

				new BukkitRunnable() {
					@Override
					public void run() {
						Util.msgRaw(s, "<gold>=== " + title + " (balance v" + version
								+ (regionType != null ? ", " + regionType : "")
								+ (playerClass != null ? ", " + playerClass : "") + ") ===");
						if (lines.isEmpty()) {
							Util.msgRaw(s, "<yellow>No mobs recorded.");
							return;
						}
						for (String line : lines) {
							Util.msgRaw(s, line);
						}
						String baseCommand = command;
						if (regionType != null) baseCommand += " " + regionType;
						if (playerClass != null) baseCommand += " " + playerClass;
						sendPageControls(s, baseCommand, filters);
					}
				}.runTask(NeoRogue.inst());
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}

	private static void queryMobLeaderboard(Connection con, int version, String regionType, String playerClass,
			Set<String> mobIdWhitelist, AnalyticsFilters filters, ArrayList<String> lines) throws SQLException {
		HashMap<String, Long> averageWinTimes = queryAverageWinTimes(con, version, regionType, playerClass,
				mobIdWhitelist, filters);
		StringBuilder sql = new StringBuilder("SELECT fm.mobId, COUNT(DISTINCT fm.fightId) AS fights,"
				+ " SUM(fm.damageDealt) AS total, AVG(fm.damageDealt) AS avgDmg, AVG(fm.outcome) AS winrate"
				+ " FROM neorogue_analytics_fight_mobs fm"
				+ " JOIN neorogue_analytics_fights f ON f.fightId = fm.fightId WHERE fm.balanceVersion = ?");
		if (regionType != null) sql.append(" AND fm.regionType = ?");
		if (playerClass != null) sql.append(" AND fm.playerClass = ?");
		if (mobIdWhitelist != null && !mobIdWhitelist.isEmpty()) {
			sql.append(" AND fm.mobId IN (");
			for (int i = 0; i < mobIdWhitelist.size(); i++) sql.append(i == 0 ? "?" : ",?");
			sql.append(")");
		}
		filters.appendWhere(sql);
		sql.append(" GROUP BY fm.mobId");
		if (filters.filterLowSamples()) {
			sql.append(" HAVING COUNT(DISTINCT fm.fightId) >= ").append(MIN_SAMPLES);
		}

		ArrayList<String> top = new ArrayList<String>();
		try (PreparedStatement ps = con.prepareStatement(sql.toString() + " ORDER BY avgDmg DESC" + pageClause(filters) + ";")) {
			int idx = bindMobLeaderboardParams(ps, version, regionType, playerClass, mobIdWhitelist);
			filters.bind(ps, idx);
			collectMobLeaderboardRows(ps, top, LEADERBOARD_LIMIT + 1, averageWinTimes);
		}
		filters.setHasNextPage(trimPage(top));
		if (top.isEmpty()) return;

		lines.add("<red>Most damaging:");
		lines.addAll(top);

		ArrayList<String> bottom = new ArrayList<String>();
		try (PreparedStatement ps = con.prepareStatement(sql.toString() + " ORDER BY avgDmg ASC" + pageClause(filters) + ";")) {
			int idx = bindMobLeaderboardParams(ps, version, regionType, playerClass, mobIdWhitelist);
			filters.bind(ps, idx);
			collectMobLeaderboardRows(ps, bottom, LEADERBOARD_LIMIT + 1, averageWinTimes);
		}
		filters.setHasNextPage(filters.hasNextPage() | trimPage(bottom));
		lines.add("<green>Least damaging:");
		lines.addAll(bottom);
	}

	private static HashMap<String, Long> queryAverageWinTimes(Connection con, int version, String regionType,
			String playerClass, Set<String> mobIdWhitelist, AnalyticsFilters filters) throws SQLException {
		StringBuilder sql = new StringBuilder("SELECT appearances.mobId, AVG(f.durationMs) AS avgWinMs FROM ("
				+ " SELECT DISTINCT fm.mobId, fm.fightId FROM neorogue_analytics_fight_mobs fm"
				+ " JOIN neorogue_analytics_fights f ON f.fightId = fm.fightId"
				+ " WHERE fm.balanceVersion = ?");
		if (regionType != null) sql.append(" AND fm.regionType = ?");
		if (playerClass != null) sql.append(" AND fm.playerClass = ?");
		if (mobIdWhitelist != null && !mobIdWhitelist.isEmpty()) {
			sql.append(" AND fm.mobId IN (");
			for (int i = 0; i < mobIdWhitelist.size(); i++) sql.append(i == 0 ? "?" : ",?");
			sql.append(")");
		}
		filters.appendWhere(sql);
		sql.append(") appearances JOIN neorogue_analytics_fights f ON f.fightId = appearances.fightId"
				+ " WHERE f.outcome = 1 GROUP BY appearances.mobId;");

		HashMap<String, Long> times = new HashMap<String, Long>();
		try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
			int idx = bindMobLeaderboardParams(ps, version, regionType, playerClass, mobIdWhitelist);
			filters.bind(ps, idx);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) times.put(rs.getString("mobId"), Math.round(rs.getDouble("avgWinMs")));
			}
		}
		return times;
	}

	private static int bindMobLeaderboardParams(PreparedStatement ps, int version, String regionType,
			String playerClass, Set<String> mobIdWhitelist) throws SQLException {
		int idx = 1;
		ps.setInt(idx++, version);
		if (regionType != null) ps.setString(idx++, regionType);
		if (playerClass != null) ps.setString(idx++, playerClass);
		if (mobIdWhitelist != null && !mobIdWhitelist.isEmpty()) {
			for (String id : mobIdWhitelist) ps.setString(idx++, id);
		}
		return idx;
	}

	private static void collectMobLeaderboardRows(PreparedStatement ps, ArrayList<String> rows, int limit,
			HashMap<String, Long> averageWinTimes) throws SQLException {
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next() && rows.size() < limit) {
				int fights = rs.getInt("fights");
				double avgDmg = rs.getDouble("avgDmg");
				double winrate = 100.0 * rs.getDouble("winrate");
				String mobId = rs.getString("mobId");
				Long avgWinMs = averageWinTimes.get(mobId);
				String avgTime = avgWinMs == null ? "" : " | " + formatDuration(avgWinMs);
				rows.add("  <yellow>" + df.format(avgDmg) + "</yellow> <white>" + mobId
						+ "</white> <gray>| " + fights + "F | " + df.format(winrate) + "% WR"
						+ avgTime + lowSampleMarker(fights));
			}
		}
	}

	private static String formatDuration(long durationMs) {
		long totalSeconds = Math.round(durationMs / 1000.0);
		return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
	}

	// Per-mob detail: appearances, average/total damage to the party, party winrate, and the damage
	// type breakdown for a single mob id.
	public static void mob(CommandSender s, String mobId, int version, AnalyticsFilters filters) {
		new BukkitRunnable() {
			@Override
			public void run() {
				ArrayList<String> lines = new ArrayList<String>();
				try (Connection con = SQLManager.getConnection("NeoRogue")) {
					if (queryMobDetail(con, mobId, version, filters, lines)) {
						queryMobByClass(con, mobId, version, filters, lines);
						queryMobDamageTypes(con, mobId, version, filters, lines);
					}
					addReportMeta(lines, filters);
				}
				catch (SQLException ex) {
					lines.clear();
					lines.add("<red>Failed to query analytics (see console).");
					ex.printStackTrace();
				}

				new BukkitRunnable() {
					@Override
					public void run() {
						Util.msgRaw(s, "<gold>=== Mob Analytics: <yellow>" + mobId + "</yellow> (balance v" + version
								+ ") ===");
						if (lines.isEmpty()) {
							Util.msgRaw(s, "<yellow>No recorded damage for this mob.");
							return;
						}
						for (String line : lines) {
							Util.msgRaw(s, line);
						}
					}
				}.runTask(NeoRogue.inst());
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}

	private static boolean queryMobDetail(Connection con, String mobId, int version, AnalyticsFilters filters,
			ArrayList<String> lines) throws SQLException {
		StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT fm.fightId) AS fights,"
				+ " SUM(fm.damageDealt) AS total, AVG(fm.damageDealt) AS avgDmg"
				+ " FROM neorogue_analytics_fight_mobs fm"
				+ " JOIN neorogue_analytics_fights f ON f.fightId = fm.fightId"
				+ " WHERE fm.mobId = ? AND fm.balanceVersion = ?");
		filters.appendWhere(sql);
		sql.append(";");
		boolean hasData = false;
		try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
			ps.setString(1, mobId);
			ps.setInt(2, version);
			filters.bind(ps, 3);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next() && rs.getInt("fights") > 0) {
					if (filters.filterLowSamples() && rs.getInt("fights") < MIN_SAMPLES) return false;
					hasData = true;
					int fights = rs.getInt("fights");
					lines.add("  <white>Appearances:</white> <yellow>" + fights + "</yellow> fights"
							+ lowSampleMarker(fights));
					lines.add("  <white>Avg Damage/player:</white> <yellow>" + df.format(rs.getDouble("avgDmg"))
							+ "</yellow> <gray>| Total:</gray> " + df.format(rs.getDouble("total")));
				}
			}
		}
		if (!hasData) return false;

		// Party winrate over distinct fights (outcome is identical for every per-player row of a fight).
		StringBuilder wrSql = new StringBuilder("SELECT AVG(t.outcome) AS winrate FROM (SELECT fm.fightId,"
				+ " MAX(fm.outcome) AS outcome FROM neorogue_analytics_fight_mobs fm"
				+ " JOIN neorogue_analytics_fights f ON f.fightId = fm.fightId"
				+ " WHERE fm.mobId = ? AND fm.balanceVersion = ?");
		filters.appendWhere(wrSql);
		wrSql.append(" GROUP BY fm.fightId) t;");
		try (PreparedStatement ps = con.prepareStatement(wrSql.toString())) {
			ps.setString(1, mobId);
			ps.setInt(2, version);
			filters.bind(ps, 3);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					lines.add("  <white>Party Winrate:</white> <yellow>" + df.format(100.0 * rs.getDouble("winrate")) + "%");
				}
			}
		}
		return true;
	}

	// Per-class breakdown: average damage this mob deals to a single player of each class, plus the
	// winrate of fights that class was present for (weighted by class headcount).
	private static void queryMobByClass(Connection con, String mobId, int version, AnalyticsFilters filters,
			ArrayList<String> lines) throws SQLException {
		StringBuilder sql = new StringBuilder("SELECT fm.playerClass, COUNT(*) AS players,"
				+ " AVG(fm.damageDealt) AS avgDmg, SUM(fm.damageDealt) AS total, AVG(fm.outcome) AS winrate"
				+ " FROM neorogue_analytics_fight_mobs fm"
				+ " JOIN neorogue_analytics_fights f ON f.fightId = fm.fightId"
				+ " WHERE fm.mobId = ? AND fm.balanceVersion = ?");
		filters.appendWhere(sql);
		sql.append(" GROUP BY fm.playerClass ORDER BY avgDmg DESC;");
		try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
			ps.setString(1, mobId);
			ps.setInt(2, version);
			filters.bind(ps, 3);
			try (ResultSet rs = ps.executeQuery()) {
				boolean header = false;
				while (rs.next()) {
					if (!header) {
						lines.add("<gold>By class (avg damage/player):");
						header = true;
					}
					lines.add("  <aqua>" + rs.getString("playerClass") + ":</aqua> <yellow>"
							+ df.format(rs.getDouble("avgDmg")) + "</yellow> <gray>avg/player ("
							+ rs.getInt("players") + " players, " + df.format(100.0 * rs.getDouble("winrate")) + "% wr)");
				}
			}
		}
	}

	private static void queryMobDamageTypes(Connection con, String mobId, int version, AnalyticsFilters filters,
			ArrayList<String> lines) throws SQLException {
		StringBuilder sql = new StringBuilder("SELECT fm.damageType, SUM(fm.amount) AS total, AVG(fm.amount) AS avgAmt"
				+ " FROM neorogue_analytics_fight_mob_damage fm"
				+ " JOIN neorogue_analytics_fights f ON f.fightId = fm.fightId"
				+ " WHERE fm.mobId = ? AND fm.balanceVersion = ?");
		filters.appendWhere(sql);
		sql.append(" GROUP BY fm.damageType ORDER BY total DESC;");
		try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
			ps.setString(1, mobId);
			ps.setInt(2, version);
			filters.bind(ps, 3);
			try (ResultSet rs = ps.executeQuery()) {
				boolean header = false;
				while (rs.next()) {
					if (!header) {
						lines.add("<gold>Damage by type:");
						header = true;
					}
					lines.add("  <aqua>" + rs.getString("damageType") + ":</aqua> <yellow>"
							+ df.format(rs.getDouble("avgAmt")) + "</yellow> <gray>avg (Total: "
							+ df.format(rs.getDouble("total")) + ")");
				}
			}
		}
	}

	private static void queryChanceLeaderboard(Connection con, int version, String setId, String playerClass,
			AnalyticsFilters filters, ArrayList<String> lines) throws SQLException {
		// Pickrate (picked/valid) comes straight from the choice rows. Winrate joins each picked choice
		// to its run's final outcome (neorogue_analytics_runs) so we can see how each option correlates
		// with actually winning the run. Runs that were abandoned (no outcome row) are simply excluded
		// from the winrate average via the LEFT JOIN producing NULL wons.
		StringBuilder sql = new StringBuilder("SELECT c.setId AS setId, c.stageId AS stageId, c.choiceIndex AS choiceIndex,"
				+ " MAX(c.choiceLabel) AS label, SUM(c.valid) AS valid, SUM(c.picked) AS picked,"
				+ " (SUM(c.picked) / SUM(c.valid)) AS rate,"
				+ " SUM(CASE WHEN c.picked = 1 AND r.won IS NOT NULL THEN 1 ELSE 0 END) AS wrSamples,"
				+ " SUM(CASE WHEN c.picked = 1 AND r.won = 1 THEN 1 ELSE 0 END) AS wrWins"
				+ " FROM neorogue_analytics_chance_choices c"
				+ " LEFT JOIN neorogue_analytics_runs r ON r.runId = c.runId"
				+ " WHERE c.balanceVersion = ?");
		if (setId != null) sql.append(" AND c.setId = ?");
		if (playerClass != null) sql.append(" AND c.playerClass = ?");
		filters.appendWhere(sql);
		sql.append(" GROUP BY c.setId, c.stageId, c.choiceIndex");
		if (filters.filterLowSamples()) {
			sql.append(" HAVING SUM(c.valid) >= ").append(MIN_SAMPLES)
					.append(" AND (wrSamples = 0 OR wrSamples >= ").append(MIN_SAMPLES).append(")");
		}
		sql.append(" ORDER BY c.setId, c.stageId, rate DESC").append(pageClause(filters)).append(";");

		try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
			int idx = 1;
			ps.setInt(idx++, version);
			if (setId != null) ps.setString(idx++, setId);
			if (playerClass != null) ps.setString(idx++, playerClass);
			filters.bind(ps, idx);
			try (ResultSet rs = ps.executeQuery()) {
				String currentSet = null;
				int row = 0;
				while (rs.next()) {
					if (++row > LEADERBOARD_LIMIT) {
						filters.setHasNextPage(true);
						break;
					}
					String set = rs.getString("setId");
					if (!set.equals(currentSet)) {
						lines.add("<gold>" + set + ":");
						currentSet = set;
					}
					int valid = rs.getInt("valid");
					int picked = rs.getInt("picked");
					double rate = valid > 0 ? (100.0 * picked / valid) : 0;
					int wrSamples = rs.getInt("wrSamples");
					int wrWins = rs.getInt("wrWins");
					String wr = wrSamples > 0
							? " <gray>|</gray> <green>" + df.format(100.0 * wrWins / wrSamples) + "% WR</green> <gray>" + wrWins + "/"
									+ wrSamples
							: " <dark_gray>| no outcomes";
					lines.add("  <aqua>" + rs.getString("stageId") + "</aqua> <white>" + rs.getString("label")
							+ "</white> <yellow>" + df.format(rate) + "%</yellow> <gray>" + picked + "/" + valid
							+ wr + lowSampleMarker(Math.min(valid, wrSamples == 0 ? valid : wrSamples)));
				}
			}
		}
	}
}
