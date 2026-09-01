package me.neoblade298.neorogue.player.caravan;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;

import me.neoblade298.neocore.shared.io.Section;

// A named set of vanilla materials a player is permitted to store in their cargo. The "default"
// package (see SellablePackageRegistry.DEFAULT_ID) is available to everyone; other packages are
// granted via caravan upgrades. Configured in sellables.yml.
public class SellablePackage {
	public static final String PERMISSION_PREFIX = "neorogue.sellables.";
	private final String id;
	private final String display;
	private final String permission;
	private final Set<Material> materials = new HashSet<Material>();

	public SellablePackage(String id, Section sec) {
		this.id = id;
		this.display = sec.getString("display", id);
		this.permission = PERMISSION_PREFIX + id.toLowerCase(Locale.ROOT);
		List<String> mats = sec.getStringList("materials");
		if (mats != null) {
			for (String matName : mats) {
				Material mat = Material.matchMaterial(matName);
				if (mat != null) {
					materials.add(mat);
					continue;
				}

				// Unmatched entries are treated as vanilla item tag names. The config intentionally uses
				// plain names such as LOGS rather than namespaced values containing a colon.
				Tag<Material> tag = Bukkit.getTag(Tag.REGISTRY_ITEMS,
						NamespacedKey.minecraft(matName.toLowerCase(Locale.ROOT)), Material.class);
				if (tag != null) {
					materials.addAll(tag.getValues());
					continue;
				}

				Bukkit.getLogger().warning("[NeoRogue] Unknown material or item tag '" + matName
						+ "' in sellable package " + id);
			}
		}
	}

	public String getId() {
		return id;
	}

	public String getDisplay() {
		return display;
	}

	public String getPermission() {
		return permission;
	}

	public Set<Material> getMaterials() {
		return materials;
	}

	public boolean contains(Material mat) {
		return materials.contains(mat);
	}
}
