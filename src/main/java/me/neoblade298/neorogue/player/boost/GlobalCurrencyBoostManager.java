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

// Manages server-wide timed currency boosts captured into each new run.
public class GlobalCurrencyBoostManager {
	private static final ArrayList<CurrencyBoost> boosts = new ArrayList<CurrencyBoost>();

	public static void load() {
		boosts.clear();
		try (Connection con = NeoCore.getConnection("NeoRogue-PlayerData");
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM neorogue_global_currencyboosts;")) {
			while (rs.next()) {
				try {
					CurrencyBoostType type = CurrencyBoostType.valueOf(rs.getString("type"));
					CurrencyBoost boost = new CurrencyBoost(type, rs.getLong("remaining"));
					if (!boost.isExpired()) boosts.add(boost);
				} catch (IllegalArgumentException ex) {
					// Unknown boost type, skip
				}
			}
		} catch (SQLException ex) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to load global currency boosts");
			ex.printStackTrace();
		}
	}

	public static boolean addGlobalBoost(CurrencyBoostType type, long durationSeconds) {
		if (type.getDurationType() != BoostDurationType.TIME) {
			throw new IllegalArgumentException("Global boosts must use the TIME duration type");
		}
		boosts.removeIf(CurrencyBoost::isExpired);
		long expiry = System.currentTimeMillis() + durationSeconds * 1000L;
		CurrencyBoost existing = null;
		for (CurrencyBoost boost : boosts) {
			if (boost.getType() == type) {
				existing = boost;
				break;
			}
		}
		if (existing == null) boosts.add(new CurrencyBoost(type, expiry));
		else existing.setRemaining(existing.getRemaining() + durationSeconds * 1000L);
		saveAsync();
		return existing != null;
	}

	public static void clear() {
		boosts.clear();
		saveAsync();
	}

	public static List<CurrencyBoost> getGlobalBoosts() {
		boosts.removeIf(CurrencyBoost::isExpired);
		return List.copyOf(boosts);
	}

	public static long getGlobalBoostRemaining(CurrencyBoostType type) {
		boosts.removeIf(CurrencyBoost::isExpired);
		for (CurrencyBoost boost : boosts) {
			if (boost.getType() == type) return boost.getRemainingDuration();
		}
		return 0;
	}

	private static void saveAsync() {
		final ArrayList<CurrencyBoost> snapshot = new ArrayList<CurrencyBoost>(boosts);
		new BukkitRunnable() {
			@Override
			public void run() {
				try (Connection con = NeoCore.getConnection("NeoRogue-PlayerData")) {
					try (Statement clear = con.createStatement()) {
						clear.execute("DELETE FROM neorogue_global_currencyboosts;");
					}
					if (snapshot.isEmpty()) return;
					SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.REPLACE, "neorogue_global_currencyboosts");
					for (CurrencyBoost boost : snapshot) {
						sql.addValue("type", boost.getType().name())
								.addValue("remaining", boost.getRemaining())
								.addRow();
					}
					try (PreparedStatement ps = sql.build(con)) {
						ps.executeBatch();
					}
				} catch (SQLException ex) {
					Bukkit.getLogger().warning("[NeoRogue] Failed to save global currency boosts");
					ex.printStackTrace();
				}
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}
}
