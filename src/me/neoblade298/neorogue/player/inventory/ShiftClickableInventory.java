package me.neoblade298.neorogue.player.inventory;

import org.bukkit.inventory.ItemStack;

public interface ShiftClickableInventory {
	public boolean canShiftClickIn(ItemStack item);
	public void handleShiftClickIn(ItemStack item);
}
