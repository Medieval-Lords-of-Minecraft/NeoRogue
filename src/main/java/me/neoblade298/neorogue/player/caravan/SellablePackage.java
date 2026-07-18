package me.neoblade298.neorogue.player.caravan;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import me.neoblade298.neocore.shared.io.Section;

// A named set of vanilla materials a player is permitted to store in their cargo. The "default"
// package (see SellablePackageRegistry.DEFAULT_ID) is available to everyone; other packages are
// granted via caravan upgrades. Configured in sellables.yml.
public class SellablePackage {
	private final String id;
	private final String display;
	private final Set<Material> materials = new HashSet<Material>();

	public SellablePackage(String id, Section sec) {
		this.id = id;
		this.display = sec.getString("display", id);
		List<String> mats = sec.getStringList("materials");
		if (mats != null) {
			for (String matName : mats) {
				Material mat = Material.matchMaterial(matName);
				if (mat == null) {
					Bukkit.getLogger().warning("[NeoRogue] Unknown material '" + matName + "' in sellable package " + id);
					continue;
				}
				materials.add(mat);
			}
		}
	}

	public String getId() {
		return id;
	}

	public String getDisplay() {
		return display;
	}

	public Set<Material> getMaterials() {
		return materials;
	}

	public boolean contains(Material mat) {
		return materials.contains(mat);
	}
}
