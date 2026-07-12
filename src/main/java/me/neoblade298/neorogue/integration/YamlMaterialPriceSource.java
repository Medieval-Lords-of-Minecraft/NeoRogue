package me.neoblade298.neorogue.integration;

import java.io.File;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.shared.io.Section;
import me.neoblade298.neorogue.NeoRogue;

/**
 * {@link MaterialPriceSource} backed by {@code prices.yml} in the plugin data folder.
 * <p>
 * Unknown material names are logged and skipped so a bad entry never breaks the rest
 * of the config.
 */
public class YamlMaterialPriceSource implements MaterialPriceSource {
	private final Map<Material, Double> prices = new EnumMap<>(Material.class);

	@Override
	public void load() {
		prices.clear();

		NeoCore.loadFiles(new File(NeoRogue.inst().getDataFolder(), "prices.yml"), (yml, file) -> {
			Section pricesSec = yml.getSection("prices");
			if (pricesSec == null) {
				Bukkit.getLogger().warning("[NeoRogue] prices.yml is missing a 'prices' section");
				return;
			}

			for (String key : pricesSec.getKeys()) {
				Material mat = Material.matchMaterial(key);
				if (mat == null) {
					Bukkit.getLogger().warning("[NeoRogue] Unknown material '" + key + "' in prices.yml, skipping");
					continue;
				}
				prices.put(mat, pricesSec.getDouble(key));
			}
		});
	}

	@Override
	public Double getPrice(Material mat) {
		return prices.get(mat);
	}

	@Override
	public Map<Material, Double> getPrices() {
		return Collections.unmodifiableMap(prices);
	}
}
