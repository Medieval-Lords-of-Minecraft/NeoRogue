package me.neoblade298.neorogue.area;

import org.bukkit.Material;

public enum NodeType {
	FIGHT(Material.REDSTONE_BLOCK), CHANCE(Material.NOTE_BLOCK), SHOP(Material.EMERALD_BLOCK), MINIBOSS(Material.OBSIDIAN), BOSS(Material.RESPAWN_ANCHOR),
	SHRINE(Material.OCHRE_FROGLIGHT), START(Material.IRON_BLOCK);
	
	private Material mat;
	
	private NodeType(Material mat) {
		this.mat = mat;
	}

	public Material getBlock() {
		return mat;
	}
}
