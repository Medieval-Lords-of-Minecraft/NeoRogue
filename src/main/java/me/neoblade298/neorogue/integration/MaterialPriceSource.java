package me.neoblade298.neorogue.integration;

import java.util.Map;

import org.bukkit.Material;

/**
 * A backend that supplies currency prices for materials.
 * <p>
 * The current implementation is {@link YamlMaterialPriceSource}, backed by
 * {@code prices.yml}. A SQL-backed source can be added later and swapped in via
 * {@link MaterialPrices#setSource(MaterialPriceSource)} without changing any
 * consumer code.
 */
public interface MaterialPriceSource {
	/** (Re)loads all prices from the backing store. */
	void load();

	/**
	 * @return the price of the material in currency units, or {@code null} if the
	 *         material has no configured price.
	 */
	Double getPrice(Material mat);

	/** @return an immutable view of all configured material prices. */
	Map<Material, Double> getPrices();
}
