package me.neoblade298.neorogue.player.caravan;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neorogue.NeoRogue;

// Loads and stores the configured sellable packages from sellables.yml. Each top-level key is a
// package id; the special "default" package is available to every player.
public class SellablePackageRegistry {
	public static final String DEFAULT_ID = "default";
	private static final LinkedHashMap<String, SellablePackage> packages = new LinkedHashMap<String, SellablePackage>();

	private SellablePackageRegistry() {
	}

	public static synchronized void reload() {
		packages.clear();
		NeoCore.loadFiles(new File(NeoRogue.inst().getDataFolder(), "sellables.yml"), (yml, file) -> {
			for (String key : yml.getKeys()) {
				try {
					packages.put(key, new SellablePackage(key, yml.getSection(key)));
				} catch (Exception e) {
					e.printStackTrace();
					Bukkit.getLogger().warning("[NeoRogue] Failed to load sellable package " + key
							+ " in file " + file.getName());
				}
			}
		});
		if (!packages.containsKey(DEFAULT_ID)) {
			Bukkit.getLogger().warning("[NeoRogue] sellables.yml has no 'default' package; "
					+ "players will only be able to deposit materials from packages they own.");
		}
	}

	public static SellablePackage get(String id) {
		return packages.get(id);
	}

	public static boolean exists(String id) {
		return packages.containsKey(id);
	}

	public static Collection<SellablePackage> getPackages() {
		return packages.values();
	}

	// Whether a material may be deposited given the player's owned package ids. The default package
	// is always available regardless of ownership.
	public static boolean canDeposit(Set<String> ownedPackageIds, Material mat) {
		SellablePackage def = packages.get(DEFAULT_ID);
		if (def != null && def.contains(mat)) return true;
		for (String id : ownedPackageIds) {
			SellablePackage pkg = packages.get(id);
			if (pkg != null && pkg.contains(mat)) return true;
		}
		return false;
	}
}
