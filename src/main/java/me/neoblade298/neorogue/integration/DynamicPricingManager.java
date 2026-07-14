package me.neoblade298.neorogue.integration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.shared.io.SQLManager;
import me.neoblade298.neocore.shared.util.SQLInsertBuilder;
import me.neoblade298.neocore.shared.util.SQLInsertBuilder.SQLAction;
import me.neoblade298.neorogue.NeoRogue;

/**
 * Dynamic pricing layer over the static base prices from {@code prices.yml}.
 * <p>
 * Every material carries a persisted price {@code multiplier} (default {@code 1.0}) stored in SQL.
 * The effective sell price a consumer sees is {@code basePrice * multiplier}, and
 * {@link MaterialPrices#getPrice(Material)} applies it automatically.
 * <p>
 * Sales are logged to {@code neorogue_material_sales} (material, amount, currency value, timestamp).
 * Once an hour {@link #updatePrices()} sums each material's currency volume over the trailing 24h,
 * computes the mean/standard deviation of that set, places each material on a negative sigmoid
 * ({@code x / (1 + |x|)}) by its z-score, and nudges its multiplier by up to +/-10%. High-volume
 * items drop in price; low-volume items rise. Multipliers are clamped to [{@value #MIN_MULTIPLIER},
 * {@value #MAX_MULTIPLIER}] x base.
 */
public class DynamicPricingManager {
	// Master switch. Disable to skip all schema/recording/update work (prices stay at base).
	public static final boolean ENABLED = true;

	// Multiplier bounds relative to a material's base price (10% floor, 10x ceiling).
	public static final double MIN_MULTIPLIER = 0.1, MAX_MULTIPLIER = 10.0;

	// Sigmoid output cap: a single hourly update can move a price by at most +/-10%.
	public static final double MAX_CHANGE = 0.10;

	// Trailing window over which sales volume is summed for the update.
	private static final long WINDOW_MS = 24L * 60L * 60L * 1000L;

	// Recompute cadence (1 hour, in server ticks).
	private static final long UPDATE_INTERVAL_TICKS = 60L * 60L * 20L;

	private static volatile boolean initialized = false;

	// Main-thread-only cache of current multipliers. Mutated only on the main thread.
	private static final Map<Material, Double> multipliers = new EnumMap<>(Material.class);

	private DynamicPricingManager() {
	}

	/** Creates schema, loads persisted multipliers, and schedules the hourly price update. */
	public static void init() {
		if (!ENABLED) return;

		new BukkitRunnable() {
			@Override
			public void run() {
				try (Connection con = SQLManager.getConnection("NeoRogue"); Statement stmt = con.createStatement()) {
					stmt.execute("CREATE TABLE IF NOT EXISTS neorogue_material_sales ("
							+ "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
							+ "material VARCHAR(64) NOT NULL,"
							+ "amount INT NOT NULL,"
							+ "value DOUBLE NOT NULL,"
							+ "ts BIGINT NOT NULL"
							+ ");");
					createIndex(stmt, "idx_material_sales_ts", "neorogue_material_sales", "ts");
					createIndex(stmt, "idx_material_sales_mat_ts", "neorogue_material_sales", "material, ts");

					stmt.execute("CREATE TABLE IF NOT EXISTS neorogue_material_price_multiplier ("
							+ "material VARCHAR(64) NOT NULL PRIMARY KEY,"
							+ "multiplier DOUBLE NOT NULL,"
							+ "updatedTs BIGINT NOT NULL"
							+ ");");

					Map<Material, Double> loaded = new EnumMap<>(Material.class);
					try (ResultSet rs = stmt.executeQuery(
							"SELECT material, multiplier FROM neorogue_material_price_multiplier;")) {
						while (rs.next()) {
							Material mat = Material.matchMaterial(rs.getString("material"));
							if (mat != null) loaded.put(mat, rs.getDouble("multiplier"));
						}
					}

					// Publish the loaded multipliers onto the main thread.
					new BukkitRunnable() {
						@Override
						public void run() {
							multipliers.clear();
							multipliers.putAll(loaded);
							initialized = true;
						}
					}.runTask(NeoRogue.inst());
				}
				catch (SQLException ex) {
					Bukkit.getLogger().warning("[NeoRogue] Failed to initialize dynamic pricing tables");
					ex.printStackTrace();
				}
			}
		}.runTaskAsynchronously(NeoRogue.inst());

		new BukkitRunnable() {
			@Override
			public void run() {
				updatePrices();
			}
		}.runTaskTimer(NeoRogue.inst(), UPDATE_INTERVAL_TICKS, UPDATE_INTERVAL_TICKS);
	}

	/** @return the current price multiplier for the material (1.0 if none stored / not yet loaded). */
	public static double getMultiplier(Material mat) {
		if (!ENABLED || mat == null) return 1.0;
		Double m = multipliers.get(mat);
		return m == null ? 1.0 : m;
	}

	/**
	 * Logs a sale of {@code amount} units of {@code mat} for a total of {@code value} currency.
	 * Fire-and-forget; the insert runs asynchronously.
	 */
	public static void recordSale(Material mat, int amount, double value) {
		if (!ENABLED || !initialized || mat == null || amount <= 0) return;
		final long ts = System.currentTimeMillis();
		final String name = mat.name();
		new BukkitRunnable() {
			@Override
			public void run() {
				try (Connection con = SQLManager.getConnection("NeoRogue")) {
					SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.INSERT, "neorogue_material_sales")
							.addValue("material", name)
							.addValue("amount", amount)
							.addValue("value", value)
							.addValue("ts", ts);
					try (PreparedStatement ps = sql.build(con)) {
						ps.executeBatch();
					}
				}
				catch (SQLException ex) {
					Bukkit.getLogger().warning("[NeoRogue] Failed to record material sale for " + name);
					ex.printStackTrace();
				}
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}

	/**
	 * Recomputes multipliers from the trailing 24h of sales and persists them. Runs asynchronously
	 * and republishes the results onto the main thread. Also prunes sales older than the window.
	 */
	public static void updatePrices() {
		if (!ENABLED || !initialized) return;
		final long cutoff = System.currentTimeMillis() - WINDOW_MS;

		new BukkitRunnable() {
			@Override
			public void run() {
				Map<Material, Double> volume = new EnumMap<>(Material.class);
				try (Connection con = SQLManager.getConnection("NeoRogue")) {
					try (PreparedStatement prune = con
							.prepareStatement("DELETE FROM neorogue_material_sales WHERE ts < ?;")) {
						prune.setLong(1, cutoff);
						prune.executeUpdate();
					}

					try (PreparedStatement ps = con.prepareStatement(
							"SELECT material, SUM(value) AS total FROM neorogue_material_sales WHERE ts >= ? GROUP BY material;")) {
						ps.setLong(1, cutoff);
						try (ResultSet rs = ps.executeQuery()) {
							while (rs.next()) {
								Material mat = Material.matchMaterial(rs.getString("material"));
								if (mat != null) volume.put(mat, rs.getDouble("total"));
							}
						}
					}

					if (volume.isEmpty()) return;

					// Metrics over the set of per-material 24h currency volumes.
					double sum = 0;
					for (double v : volume.values()) sum += v;
					double mean = sum / volume.size();
					double variance = 0;
					for (double v : volume.values()) {
						double d = v - mean;
						variance += d * d;
					}
					variance /= volume.size();
					double stddev = Math.sqrt(variance);

					Map<Material, Double> updated = new EnumMap<>(Material.class);
					long now = System.currentTimeMillis();
					SQLInsertBuilder upsert = new SQLInsertBuilder(SQLAction.REPLACE,
							"neorogue_material_price_multiplier");
					for (Map.Entry<Material, Double> e : volume.entrySet()) {
						Material mat = e.getKey();
						// Position on the sigmoid by z-score: high volume -> high x -> price drop.
						double x = stddev == 0 ? 0 : (e.getValue() - mean) / stddev;
						double sigmoid = x / (1 + Math.abs(x)); // (-1, 1)
						double change = -MAX_CHANGE * sigmoid; // negative sigmoid, capped to +/-10%
						double next = getMultiplier(mat) * (1 + change);
						next = Math.max(MIN_MULTIPLIER, Math.min(MAX_MULTIPLIER, next));
						updated.put(mat, next);
						upsert.addValue("material", mat.name()).addValue("multiplier", next)
								.addValue("updatedTs", now).addRow();
					}

					try (PreparedStatement ps = upsert.build(con)) {
						ps.executeBatch();
					}

					// Publish the recomputed multipliers onto the main thread.
					new BukkitRunnable() {
						@Override
						public void run() {
							multipliers.putAll(updated);
						}
					}.runTask(NeoRogue.inst());
				}
				catch (SQLException ex) {
					Bukkit.getLogger().warning("[NeoRogue] Failed to update dynamic material prices");
					ex.printStackTrace();
				}
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}

	private static void createIndex(Statement stmt, String name, String table, String columns) {
		try {
			stmt.execute("CREATE INDEX " + name + " ON " + table + " (" + columns + ");");
		}
		catch (SQLException ignore) {
			// Index already exists
		}
	}
}
