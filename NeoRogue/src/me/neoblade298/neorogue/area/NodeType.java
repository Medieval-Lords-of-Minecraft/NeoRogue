package me.neoblade298.neorogue.area;

import org.bukkit.Material;

public enum NodeType {
	FIGHT(Material.REDSTONE_BLOCK),
	EVENT(Material.MAGENTA_GLAZED_TERRACOTTA),
	SHOP(Material.EMERALD_BLOCK),
	MINIBOSS(Material.OBSIDIAN),
	BOSS(Material.DIAMOND_BLOCK),
	REST(Material.OCHRE_FROGLIGHT),
	TREASURE(Material.GOLD_BLOCK),
	START(Material.IRON_BLOCK);

	private Material mat;
	private NodeType(Material mat) {
		this.mat = mat;
	}
	
	public Material getBlock() {
		return mat;
	}
}
