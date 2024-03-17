package me.neoblade298.neorogue.player.inventory;

import org.bukkit.inventory.ItemStack;

public class CustomGlossaryIcon implements GlossaryIcon {
	private String id;
	private ItemStack item;
	public CustomGlossaryIcon(String id, ItemStack item) {
		this.id = id;
		this.item = item;
	}
	
	@Override
	public ItemStack getIcon() {
		return item;
	}
	
	@Override
	public String getId() {
		return id;
	}
}
