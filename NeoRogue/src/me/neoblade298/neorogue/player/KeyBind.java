package me.neoblade298.neorogue.player;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;

public enum KeyBind {
	SHIFT_LCLICK(9, 0, "Shift+LClick", CoreInventory.createButton(Material.BLUE_STAINED_GLASS_PANE, "&9Ability Slot",
					"&eBound to Shift+LClick", "&7Drag an ability here to bind it!")),
	SHIFT_RCLICK(10, 1, "Shift+RClick", CoreInventory.createButton(Material.BLUE_STAINED_GLASS_PANE, "&9Ability Slot",
			"&eBound to Shift+RClick", "&7Drag an ability here to bind it!")),
	SHIFT_DROP(11, 2, "Shift+Drop", CoreInventory.createButton(Material.BLUE_STAINED_GLASS_PANE, "&9Ability Slot",
			"&eBound to Shift+Drop", "&7Drag an ability here to bind it!")),
	SHIFT_SWAP(12, 3, "Shift+Swap", CoreInventory.createButton(Material.BLUE_STAINED_GLASS_PANE, "&9Ability Slot",
			"&eBound to Shift+Swap", "&7Drag an ability here to bind it!")),
	DROP(13, 4, "Drop", CoreInventory.createButton(Material.BLUE_STAINED_GLASS_PANE, "&9Ability Slot",
			"&eBound to Drop", "&7Drag an ability here to bind it!")),
	SWAP(14, 5, "Swap", CoreInventory.createButton(Material.BLUE_STAINED_GLASS_PANE, "&9Ability Slot",
			"&eBound to Swap", "&7Drag an ability here to bind it!")),
	UP_RCLICK(15, 6, "Look up+RClick", CoreInventory.createButton(Material.BLUE_STAINED_GLASS_PANE, "&9Ability Slot",
			"&eBound to Look up+RClick", "&7Drag an ability here to bind it!")),
	DOWN_RCLICK(16, 7, "Look down+RClick", CoreInventory.createButton(Material.BLUE_STAINED_GLASS_PANE, "&9Ability Slot",
			"&eBound to Look down+RClick", "&7Drag an ability here to bind it!"));
	
	private int invSlot, dataSlot;
	private ItemStack item;
	private String display;
	private KeyBind(int invSlot, int dataSlot, String display, ItemStack item) {
		this.invSlot = invSlot;
		this.dataSlot = dataSlot;
		this.item = item;
		this.display = display;
	}
	
	public int getInventorySlot() {
		return invSlot;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public int getDataSlot() {
		return dataSlot;
	}
	
	public String getDisplay() {
		return display;
	}
	
	public static KeyBind getBindFromSlot(int slot) {
		switch (slot) {
		case 9: return SHIFT_LCLICK;
		case 10: return SHIFT_RCLICK;
		case 11: return SHIFT_DROP;
		case 12: return SHIFT_SWAP;
		case 13: return DROP;
		case 14: return SWAP;
		case 15: return UP_RCLICK;
		case 16: return DOWN_RCLICK;
		default: return SHIFT_LCLICK;
		}
	}
}
