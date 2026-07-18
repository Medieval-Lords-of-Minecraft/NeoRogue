package me.neoblade298.neorogue.player.caravan;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;

import org.bukkit.Bukkit;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neorogue.NeoRogue;

// Loads and stores the configured caravan upgrades from caravan.yml. Each top-level key is an
// upgrade id.
public class CaravanUpgradeRegistry {
	private static final LinkedHashMap<String, CaravanUpgrade> upgrades = new LinkedHashMap<String, CaravanUpgrade>();

	private CaravanUpgradeRegistry() {
	}

	public static synchronized void reload() {
		upgrades.clear();
		NeoCore.loadFiles(new File(NeoRogue.inst().getDataFolder(), "caravan.yml"), (yml, file) -> {
			for (String key : yml.getKeys()) {
				try {
					upgrades.put(key, new CaravanUpgrade(key, yml.getSection(key)));
				} catch (Exception e) {
					e.printStackTrace();
					Bukkit.getLogger().warning("[NeoRogue] Failed to load caravan upgrade " + key
							+ " in file " + file.getName());
				}
			}
		});
	}

	public static CaravanUpgrade get(String id) {
		return upgrades.get(id);
	}

	public static Collection<CaravanUpgrade> getUpgrades() {
		return upgrades.values();
	}
}
