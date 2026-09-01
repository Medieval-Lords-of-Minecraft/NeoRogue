package me.neoblade298.neorogue.player.caravan;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neorogue.NeoRogue;

// Loads and stores the configured sellable packages from sellables.yml. Each top-level key is a
// package id; the special "default" package is available to every player.
public class SellablePackageRegistry {
	public static final String DEFAULT_ID = "default";
	private static final LinkedHashMap<String, SellablePackage> packages = new LinkedHashMap<String, SellablePackage>();
	private static final Set<String> registeredPermissions = new HashSet<String>();

	private SellablePackageRegistry() {
	}

	public static synchronized void reload() {
		for (String permission : registeredPermissions) {
			Bukkit.getPluginManager().removePermission(permission);
		}
		registeredPermissions.clear();
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
		registerPermissions();
	}

	private static void registerPermissions() {
		for (SellablePackage pkg : packages.values()) {
			if (DEFAULT_ID.equals(pkg.getId())) continue;
			String name = pkg.getPermission();
			Permission permission = Bukkit.getPluginManager().getPermission(name);
			if (permission == null) {
				permission = new Permission(name, "Grants access to the " + pkg.getId()
						+ " NeoRogue sellable package", PermissionDefault.FALSE);
				Bukkit.getPluginManager().addPermission(permission);
				registeredPermissions.add(name);
			} else if (permission.getDefault() != PermissionDefault.FALSE) {
				permission.setDefault(PermissionDefault.FALSE);
				Bukkit.getPluginManager().recalculatePermissionDefaults(permission);
			}
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

	// Whether a material may be deposited. The default package is available to everyone; each other
	// package accepts either its owned-package flag or its automatic permission.
	public static boolean canDeposit(Set<String> ownedPackageIds, Player player, Material mat) {
		SellablePackage def = packages.get(DEFAULT_ID);
		if (def != null && def.contains(mat)) return true;
		for (SellablePackage pkg : packages.values()) {
			if (DEFAULT_ID.equals(pkg.getId()) || !pkg.contains(mat)) continue;
			if (ownedPackageIds.contains(pkg.getId())
					|| (player != null && player.hasPermission(pkg.getPermission()))) return true;
		}
		return false;
	}
}
