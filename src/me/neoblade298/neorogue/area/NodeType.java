package me.neoblade298.neorogue.area;

import org.bukkit.Material;
import org.bukkit.map.MapCursor;

public enum NodeType {
	FIGHT(Material.REDSTONE_BLOCK, MapCursor.Type.BANNER_RED), CHANCE(Material.NOTE_BLOCK, MapCursor.Type.TEMPLE),
	SHOP(Material.EMERALD_BLOCK, MapCursor.Type.MANSION), MINIBOSS(Material.OBSIDIAN, MapCursor.Type.WHITE_CROSS),
	BOSS(Material.RESPAWN_ANCHOR, MapCursor.Type.RED_X),
	SHRINE(Material.OCHRE_FROGLIGHT, MapCursor.Type.BANNER_YELLOW),
	START(Material.IRON_BLOCK, MapCursor.Type.BLUE_POINTER);
	
	private Material mat;
	private MapCursor.Type cursor;
	
	private NodeType(Material mat, MapCursor.Type cursor) {
		this.mat = mat;
		this.cursor = cursor;
	}

	public Material getBlock() {
		return mat;
	}

	public MapCursor.Type getCursor() {
		return cursor;
	}
}
