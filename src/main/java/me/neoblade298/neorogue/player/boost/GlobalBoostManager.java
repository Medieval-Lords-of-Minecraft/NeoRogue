package me.neoblade298.neorogue.player.boost;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.shared.util.SQLInsertBuilder;
import me.neoblade298.neocore.shared.util.SQLInsertBuilder.SQLAction;
import me.neoblade298.neorogue.NeoRogue;

// Manages server-wide exp boosts that apply to every player's run. Global boosts are
// always time-based; their contribution is captured at run start alongside personal
// boosts. State is persisted in the neorogue_global_expboosts table.
public class GlobalBoostManager {
	private static final ArrayList<ExpBoost> boosts = new ArrayList<ExpBoost>();

	// Loads active global boosts from storage. Should be called once at startup.
	public static void load() {
		boosts.clear();
		try (Connection con = NeoCore.getConnection("NeoRogue-PlayerData");
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM neorogue_global_expboosts;")) {
			while (rs.next()) {
				try {
					ExpBoostType type = ExpBoostType.valueOf(rs.getString("type"));
					ExpBoost boost = new ExpBoost(type, rs.getLong("remaining"));
					if (!boost.isExpired()) boosts.add(boost);
				} catch (IllegalArgumentException ex) {
					// Unknown boost type, skip
				}
			}
		} catch (SQLException ex) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to load global exp boosts");
			ex.printStackTrace();
		}
	}

	// Grants or extends a global boost. Only TIME-based types are allowed; RUNS types
	// are rejected and return false. Extends to the later expiry if one already exists.
	public static boolean addGlobalBoost(ExpBoostType type, long durationSeconds) {
		if (type.getDurationType() != BoostDurationType.TIME) return false;
		long expiry = System.currentTimeMillis() + durationSeconds * 1000L;
		ExpBoost existing = null;
		for (ExpBoost b : boosts) {
			if (b.getType() == type) {
				existing = b;
				break;
			}
		}
		if (existing == null) {
			boosts.add(new ExpBoost(type, expiry));
		} else {
			existing.setRemaining(Math.max(existing.getRemaining(), expiry));
		}
		saveAsync();
		return true;
	}

	// Removes all active global boosts.
	public static void clear() {
		boosts.clear();
		saveAsync();
	}

	// Returns the combined additive bonus from all active global boosts (e.g. 0.30 for
	// a single +30% boost, 0.0 if none). Prunes expired boosts.
	public static double getGlobalBoostBonus() {
		boosts.removeIf(ExpBoost::isExpired);
		double bonus = 0.0;
		for (ExpBoost b : boosts) {
			bonus += b.getMultiplier();
		}
		return bonus;
	}

	public static List<ExpBoost> getGlobalBoosts() {
		boosts.removeIf(ExpBoost::isExpired);
		return boosts;
	}

	private static void saveAsync() {
		final ArrayList<ExpBoost> snapshot = new ArrayList<>(boosts);
		new BukkitRunnable() {
			@Override
			public void run() {
				try (Connection con = NeoCore.getConnection("NeoRogue-PlayerData")) {
					try (Statement clear = con.createStatement()) {
						clear.execute("DELETE FROM neorogue_global_expboosts;");
					}
					if (snapshot.isEmpty()) return;
					SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.REPLACE, "neorogue_global_expboosts");
					for (ExpBoost boost : snapshot) {
						sql.addValue("type", boost.getType().name())
								.addValue("remaining", boost.getRemaining())
								.addRow();
					}
					try (PreparedStatement ps = sql.build(con)) {
						ps.executeBatch();
					}
				} catch (SQLException ex) {
					Bukkit.getLogger().warning("[NeoRogue] Failed to save global exp boosts");
					ex.printStackTrace();
				}
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}
}
