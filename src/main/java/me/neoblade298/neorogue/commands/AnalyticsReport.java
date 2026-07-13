package me.neoblade298.neorogue.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.io.SQLManager;
import me.neoblade298.neorogue.NeoRogue;

// Runs and prints aggregated effectiveness analytics from the per-fight fact tables. Invoked by the
// /nrlytics subcommands, which handle argument parsing; each method here just reports parsed args.
public class AnalyticsReport {
	private static final DecimalFormat df = new DecimalFormat("#.##");
	private static final int MIN_OFFERS = 10;
	private static final int LEADERBOARD_LIMIT = 10;

	private AnalyticsReport() {}

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
				+ " FROM neorogue_fight_equipment WHERE equipmentId = ? AND balanceVersion = ? GROUP BY upgraded;";
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
							+ wins + " wins)");
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
				+ " FROM neorogue_fight_equipment_status WHERE equipmentId = ? AND balanceVersion = ?"
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
							+ " <gray>(" + df.format(winrate) + "% wr, " + n + ")");
				}
			}
		}
	}

	private static void queryPickrate(Connection con, String id, int version, ArrayList<String> lines) throws SQLException {
		String sql = "SELECT source, upgraded, COUNT(*) AS offered, SUM(picked) AS picked"
				+ " FROM neorogue_equipment_offers WHERE equipmentId = ? AND balanceVersion = ?"
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
							+ "%</yellow> <gray>(" + picked + "/" + offered + " offered)");
				}
			}
		}
	}

	// Equipment pickrate leaderboard (optionally filtered to a single offer source: SHOP or REWARD).
	public static void pickrate(CommandSender s, int version, String source) {
		new BukkitRunnable() {
			@Override
			public void run() {
				ArrayList<String> lines = new ArrayList<String>();
				try (Connection con = SQLManager.getConnection("NeoRogue")) {
					queryLeaderboard(con, version, source, lines);
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
								+ (source != null ? ", " + source : "") + ") ===");
						if (lines.isEmpty()) {
							Util.msgRaw(s, "<yellow>No offers recorded with at least " + MIN_OFFERS + " samples.");
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

	private static void queryLeaderboard(Connection con, int version, String source, ArrayList<String> lines)
			throws SQLException {
		StringBuilder sql = new StringBuilder("SELECT equipmentId, upgraded, COUNT(*) AS offered, SUM(picked) AS picked,"
				+ " (SUM(picked) / COUNT(*)) AS rate FROM neorogue_equipment_offers WHERE balanceVersion = ?");
		if (source != null) sql.append(" AND source = ?");
		sql.append(" GROUP BY equipmentId, upgraded HAVING offered >= ").append(MIN_OFFERS);

		ArrayList<String[]> rows = new ArrayList<String[]>();
		try (PreparedStatement ps = con.prepareStatement(sql.toString() + " ORDER BY rate DESC;")) {
			ps.setInt(1, version);
			if (source != null) ps.setString(2, source);
			collectLeaderboardRows(ps, rows, LEADERBOARD_LIMIT);
		}
		if (rows.isEmpty()) return;

		lines.add("<green>Most picked:");
		for (String[] row : rows) lines.add(row[0]);

		rows.clear();
		try (PreparedStatement ps = con.prepareStatement(sql.toString() + " ORDER BY rate ASC;")) {
			ps.setInt(1, version);
			if (source != null) ps.setString(2, source);
			collectLeaderboardRows(ps, rows, LEADERBOARD_LIMIT);
		}
		lines.add("<red>Least picked:");
		for (String[] row : rows) lines.add(row[0]);
	}

	private static void collectLeaderboardRows(PreparedStatement ps, ArrayList<String[]> rows, int limit) throws SQLException {
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next() && rows.size() < limit) {
				boolean upgraded = rs.getInt("upgraded") == 1;
				int offered = rs.getInt("offered");
				int picked = rs.getInt("picked");
				double rate = offered > 0 ? (100.0 * picked / offered) : 0;
				String line = "  <yellow>" + df.format(rate) + "%</yellow> <white>" + rs.getString("equipmentId")
						+ (upgraded ? "+" : "") + "</white> <gray>(" + picked + "/" + offered + ")";
				rows.add(new String[] { line });
			}
		}
	}

	// Leaderboard of chance-event option pick rate, computed as picked / valid so options are only
	// counted when they were actually selectable for the player.
	public static void chance(CommandSender s, int version, String setId) {
		new BukkitRunnable() {
			@Override
			public void run() {
				ArrayList<String> lines = new ArrayList<String>();
				try (Connection con = SQLManager.getConnection("NeoRogue")) {
					queryChanceLeaderboard(con, version, setId, lines);
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
								+ (setId != null ? ", " + setId : "") + ") ===");
						if (lines.isEmpty()) {
							Util.msgRaw(s, "<yellow>No chance options recorded with at least " + MIN_OFFERS
									+ " valid samples.");
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

	// Leaderboard of mobs ranked by average damage dealt to the party per fight they appear in. Only
	// mobs that have appeared in at least MIN_OFFERS fights are listed.
	public static void mobs(CommandSender s, int version, String regionType) {
		new BukkitRunnable() {
			@Override
			public void run() {
				ArrayList<String> lines = new ArrayList<String>();
				try (Connection con = SQLManager.getConnection("NeoRogue")) {
					queryMobLeaderboard(con, version, regionType, lines);
				}
				catch (SQLException ex) {
					lines.clear();
					lines.add("<red>Failed to query analytics (see console).");
					ex.printStackTrace();
				}

				new BukkitRunnable() {
					@Override
					public void run() {
						Util.msgRaw(s, "<gold>=== Mob Damage Leaderboard (balance v" + version
								+ (regionType != null ? ", " + regionType : "") + ") ===");
						if (lines.isEmpty()) {
							Util.msgRaw(s, "<yellow>No mobs recorded in at least " + MIN_OFFERS + " fights.");
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

	private static void queryMobLeaderboard(Connection con, int version, String regionType, ArrayList<String> lines)
			throws SQLException {
		StringBuilder sql = new StringBuilder("SELECT mobId, COUNT(DISTINCT fightId) AS fights, SUM(damageDealt) AS total,"
				+ " AVG(damageDealt) AS avgDmg, AVG(outcome) AS winrate FROM neorogue_fight_mobs WHERE balanceVersion = ?");
		if (regionType != null) sql.append(" AND regionType = ?");
		sql.append(" GROUP BY mobId HAVING fights >= ").append(MIN_OFFERS);

		ArrayList<String> top = new ArrayList<String>();
		try (PreparedStatement ps = con.prepareStatement(sql.toString() + " ORDER BY avgDmg DESC;")) {
			ps.setInt(1, version);
			if (regionType != null) ps.setString(2, regionType);
			collectMobLeaderboardRows(ps, top, LEADERBOARD_LIMIT);
		}
		if (top.isEmpty()) return;

		lines.add("<red>Most damaging:");
		lines.addAll(top);

		ArrayList<String> bottom = new ArrayList<String>();
		try (PreparedStatement ps = con.prepareStatement(sql.toString() + " ORDER BY avgDmg ASC;")) {
			ps.setInt(1, version);
			if (regionType != null) ps.setString(2, regionType);
			collectMobLeaderboardRows(ps, bottom, LEADERBOARD_LIMIT);
		}
		lines.add("<green>Least damaging:");
		lines.addAll(bottom);
	}

	private static void collectMobLeaderboardRows(PreparedStatement ps, ArrayList<String> rows, int limit) throws SQLException {
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next() && rows.size() < limit) {
				int fights = rs.getInt("fights");
				double avgDmg = rs.getDouble("avgDmg");
				double winrate = 100.0 * rs.getDouble("winrate");
				rows.add("  <yellow>" + df.format(avgDmg) + "</yellow> <white>" + rs.getString("mobId")
						+ "</white> <gray>avg/player (" + fights + " fights, " + df.format(winrate) + "% party wr)");
			}
		}
	}

	// Per-mob detail: appearances, average/total damage to the party, party winrate, and the damage
	// type breakdown for a single mob id.
	public static void mob(CommandSender s, String mobId, int version) {
		new BukkitRunnable() {
			@Override
			public void run() {
				ArrayList<String> lines = new ArrayList<String>();
				try (Connection con = SQLManager.getConnection("NeoRogue")) {
					queryMobDetail(con, mobId, version, lines);
					queryMobByClass(con, mobId, version, lines);
					queryMobDamageTypes(con, mobId, version, lines);
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

	private static void queryMobDetail(Connection con, String mobId, int version, ArrayList<String> lines) throws SQLException {
		String sql = "SELECT COUNT(DISTINCT fightId) AS fights, SUM(damageDealt) AS total, AVG(damageDealt) AS avgDmg"
				+ " FROM neorogue_fight_mobs WHERE mobId = ? AND balanceVersion = ?;";
		boolean hasData = false;
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, mobId);
			ps.setInt(2, version);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next() && rs.getInt("fights") > 0) {
					hasData = true;
					int fights = rs.getInt("fights");
					lines.add("  <white>Appearances:</white> <yellow>" + fights + "</yellow> fights");
					lines.add("  <white>Avg Damage/player:</white> <yellow>" + df.format(rs.getDouble("avgDmg"))
							+ "</yellow> <gray>| Total:</gray> " + df.format(rs.getDouble("total")));
				}
			}
		}
		if (!hasData) return;

		// Party winrate over distinct fights (outcome is identical for every per-player row of a fight).
		String wrSql = "SELECT AVG(outcome) AS winrate FROM (SELECT fightId, MAX(outcome) AS outcome"
				+ " FROM neorogue_fight_mobs WHERE mobId = ? AND balanceVersion = ? GROUP BY fightId) t;";
		try (PreparedStatement ps = con.prepareStatement(wrSql)) {
			ps.setString(1, mobId);
			ps.setInt(2, version);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					lines.add("  <white>Party Winrate:</white> <yellow>" + df.format(100.0 * rs.getDouble("winrate")) + "%");
				}
			}
		}
	}

	// Per-class breakdown: average damage this mob deals to a single player of each class, plus the
	// winrate of fights that class was present for (weighted by class headcount).
	private static void queryMobByClass(Connection con, String mobId, int version, ArrayList<String> lines) throws SQLException {
		String sql = "SELECT playerClass, COUNT(*) AS players, AVG(damageDealt) AS avgDmg, SUM(damageDealt) AS total,"
				+ " AVG(outcome) AS winrate FROM neorogue_fight_mobs WHERE mobId = ? AND balanceVersion = ?"
				+ " GROUP BY playerClass ORDER BY avgDmg DESC;";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, mobId);
			ps.setInt(2, version);
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

	private static void queryMobDamageTypes(Connection con, String mobId, int version, ArrayList<String> lines)
			throws SQLException {
		String sql = "SELECT damageType, SUM(amount) AS total, AVG(amount) AS avgAmt FROM neorogue_fight_mob_damage"
				+ " WHERE mobId = ? AND balanceVersion = ? GROUP BY damageType ORDER BY total DESC;";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, mobId);
			ps.setInt(2, version);
			try (ResultSet rs = ps.executeQuery()) {
				boolean header = false;
				while (rs.next()) {
					if (!header) {
						lines.add("<gold>Damage by type:");
						header = true;
					}
					lines.add("  <aqua>" + rs.getString("damageType") + ":</aqua> <yellow>"
							+ df.format(rs.getDouble("total")) + "</yellow> <gray>total ("
							+ df.format(rs.getDouble("avgAmt")) + " avg)");
				}
			}
		}
	}

	private static void queryChanceLeaderboard(Connection con, int version, String setId, ArrayList<String> lines)
			throws SQLException {
		StringBuilder sql = new StringBuilder("SELECT setId, stageId, choiceIndex, MAX(choiceLabel) AS label,"
				+ " SUM(valid) AS valid, SUM(picked) AS picked,"
				+ " (SUM(picked) / SUM(valid)) AS rate FROM neorogue_chance_choices WHERE balanceVersion = ?");
		if (setId != null) sql.append(" AND setId = ?");
		sql.append(" GROUP BY setId, stageId, choiceIndex HAVING valid >= ").append(MIN_OFFERS)
				.append(" ORDER BY setId, stageId, rate DESC;");

		try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
			ps.setInt(1, version);
			if (setId != null) ps.setString(2, setId);
			try (ResultSet rs = ps.executeQuery()) {
				String currentSet = null;
				while (rs.next()) {
					String set = rs.getString("setId");
					if (!set.equals(currentSet)) {
						lines.add("<gold>" + set + ":");
						currentSet = set;
					}
					int valid = rs.getInt("valid");
					int picked = rs.getInt("picked");
					double rate = valid > 0 ? (100.0 * picked / valid) : 0;
					lines.add("  <aqua>" + rs.getString("stageId") + "</aqua> <white>" + rs.getString("label")
							+ "</white> <yellow>" + df.format(rate) + "%</yellow> <gray>(" + picked + "/" + valid
							+ " valid)");
				}
			}
		}
	}
}
