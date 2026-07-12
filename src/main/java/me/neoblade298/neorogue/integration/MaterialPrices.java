package me.neoblade298.neorogue.integration;

import java.util.Map;

import org.bukkit.Material;

/**
 * Static access point for material pricing.
 * <p>
 * Consumers should only ever talk to this class; the actual data comes from a
 * pluggable {@link MaterialPriceSource}. Today that is {@link YamlMaterialPriceSource}
 * ({@code prices.yml}); migrating to SQL later is just a matter of implementing a new
 * source and calling {@link #setSource(MaterialPriceSource)} before {@link #reload()}.
 * <p>
 * All prices are grounded on {@code 1 DIAMOND = getCurrencyPerDiamond()} currency.
 */
public class MaterialPrices {
	private static MaterialPriceSource source = new YamlMaterialPriceSource();

	private MaterialPrices() {
	}

	/** Swaps the backing store (e.g. YAML -> SQL). Call {@link #reload()} afterwards. */
	public static void setSource(MaterialPriceSource newSource) {
		source = newSource;
	}

	public static MaterialPriceSource getSource() {
		return source;
	}

	/** (Re)loads prices from the active source. Safe to call on plugin reload. */
	public static void reload() {
		source.load();
	}

	/** @return true if the material has a configured price. */
	public static boolean hasPrice(Material mat) {
		return source.getPrice(mat) != null;
	}

	/**
	 * @return the material's price in currency units, or {@code -1} if it has no
	 *         configured price. Use {@link #hasPrice(Material)} to distinguish an
	 *         unpriced material from a legitimately free one.
	 */
	public static double getPrice(Material mat) {
		Double price = source.getPrice(mat);
		return price == null ? -1 : price;
	}

	/** @return the material's price in currency units, or {@code def} if unpriced. */
	public static double getPrice(Material mat, double def) {
		Double price = source.getPrice(mat);
		return price == null ? def : price;
	}

	/** @return an immutable view of every configured material price. */
	public static Map<Material, Double> getAll() {
		return source.getPrices();
	}
}
