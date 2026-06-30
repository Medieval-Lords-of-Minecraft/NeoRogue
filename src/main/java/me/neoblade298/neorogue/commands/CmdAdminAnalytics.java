package me.neoblade298.neorogue.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neocore.shared.io.SQLManager;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.analytics.AnalyticsManager;

// Reports aggregated effectiveness analytics for a single equipment id from the per-fight fact tables.
public class CmdAdminAnalytics extends Subcommand {
	private static final DecimalFormat df = new DecimalFormat("#.##");
	private static final int MIN_OFFERS = 10;
	private static final int LEADERBOARD_LIMIT = 10;

	public CmdAdminAnalytics(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("equipment", false));
		args.add(new Arg("balanceVersion", false));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		if (args.length < 1) {
			Util.msg(s, "<red>Usage: /nradmin analytics <equipment> [balanceVersion]");
			Util.msg(s, "<red>       /nradmin analytics pickrate [balanceVersion] [source]");
			return;
		}

		if (args[0].equalsIgnoreCase("pickrate")) {
			runPickrateLeaderboard(s, args);
			return;
		}

		String equipmentId = args[0];
		if (equipmentId.endsWith("+")) equipmentId = equipmentId.substring(0, equipmentId.length() - 1);

		int balanceVersion = AnalyticsManager.BALANCE_VERSION;
		if (args.length > 1) {
			try {
				balanceVersion = Integer.parseInt(args[1]);
			}
			catch (NumberFormatException ex) {
				Util.msg(s, "<red>Balance version must be a number.");
				return;
			}
		}

		final String id = equipmentId;
		final int version = balanceVersion;
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
						Util.msg(s, "<gold>=== Analytics: <yellow>" + id + "</yellow> (balance v" + version + ") ===");
						if (lines.isEmpty()) {
							Util.msg(s, "<yellow>No recorded contributions for this equipment.");
							return;
						}
						for (String line : lines) {
							Util.msg(s, line);
						}
					}
				}.runTask(NeoRogue.inst());
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}

	private void queryEquipment(Connection con, String id, int version, ArrayList<String> lines) throws SQLException {
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

	private void queryStatuses(Connection con, String id, int version, ArrayList<String> lines) throws SQLException {
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

	private void queryPickrate(Connection con, String id, int version, ArrayList<String> lines) throws SQLException {
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

	private void runPickrateLeaderboard(CommandSender s, String[] args) {
		int balanceVersion = AnalyticsManager.BALANCE_VERSION;
		if (args.length > 1) {
			try {
				balanceVersion = Integer.parseInt(args[1]);
			}
			catch (NumberFormatException ex) {
				Util.msg(s, "<red>Balance version must be a number.");
				return;
			}
		}
		final String source = args.length > 2 ? args[2].toUpperCase() : null;
		final int version = balanceVersion;
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
						Util.msg(s, "<gold>=== Pickrate Leaderboard (balance v" + version
								+ (source != null ? ", " + source : "") + ") ===");
						if (lines.isEmpty()) {
							Util.msg(s, "<yellow>No offers recorded with at least " + MIN_OFFERS + " samples.");
							return;
						}
						for (String line : lines) {
							Util.msg(s, line);
						}
					}
				}.runTask(NeoRogue.inst());
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}

	private void queryLeaderboard(Connection con, int version, String source, ArrayList<String> lines)
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

	private void collectLeaderboardRows(PreparedStatement ps, ArrayList<String[]> rows, int limit) throws SQLException {
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
}
