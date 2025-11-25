package me.neoblade298.neorogue.player.inventory;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public interface ShiftClickableInventory {
	public boolean canShiftClickIn(ItemStack item);
	public void handleShiftClickIn(InventoryClickEvent ev, ItemStack item);
}
