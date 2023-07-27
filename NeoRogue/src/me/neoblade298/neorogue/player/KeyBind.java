package me.neoblade298.neorogue.player;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;

public enum KeyBind {
	SHIFT_RCLICK(9, 0, "Shift+RClick", Trigger.SHIFT_RCLICK, CoreInventory.createButton(Material.BLUE_STAINED_GLASS_PANE, "&9Ability Slot",
			"&eBound to Shift+RClick", "&7Drag an ability here to bind it!")),
	SHIFT_DROP(10, 1, "Shift+Drop", Trigger.SHIFT_DROP, CoreInventory.createButton(Material.BLUE_STAINED_GLASS_PANE, "&9Ability Slot",
			"&eBound to Shift+Drop", "&7Drag an ability here to bind it!")),
	SHIFT_SWAP(11, 2, "Shift+Swap", Trigger.SHIFT_SWAP, CoreInventory.createButton(Material.BLUE_STAINED_GLASS_PANE, "&9Ability Slot",
			"&eBound to Shift+Swap", "&7Drag an ability here to bind it!")),
	DROP(12, 3, "Drop", Trigger.DROP, CoreInventory.createButton(Material.BLUE_STAINED_GLASS_PANE, "&9Ability Slot",
			"&eBound to Drop", "&7Drag an ability here to bind it!")),
	SWAP(13, 4, "Swap", Trigger.SWAP, CoreInventory.createButton(Material.BLUE_STAINED_GLASS_PANE, "&9Ability Slot",
			"&eBound to Swap", "&7Drag an ability here to bind it!")),
	UP_RCLICK(14, 5, "Look up+RClick", Trigger.UP_RCLICK, CoreInventory.createButton(Material.BLUE_STAINED_GLASS_PANE, "&9Ability Slot",
			"&eBound to Look up+RClick", "&7Drag an ability here to bind it!")),
	DOWN_RCLICK(15, 6, "Look down+RClick", Trigger.DOWN_RCLICK, CoreInventory.createButton(Material.BLUE_STAINED_GLASS_PANE, "&9Ability Slot",
			"&eBound to Look down+RClick", "&7Drag an ability here to bind it!"));
	
	private int invSlot, dataSlot;
	private ItemStack item;
	private String display;
	private Trigger trigger;
	private KeyBind(int invSlot, int dataSlot, String display, Trigger trigger, ItemStack item) {
		this.invSlot = invSlot;
		this.dataSlot = dataSlot;
		this.item = item;
		this.display = display;
		this.trigger = trigger;
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
	
	public Trigger getTrigger() {
		return trigger;
	}
	
	public static KeyBind getBindFromData(int data) {
		switch (data) {
		case 0: return SHIFT_RCLICK;
		case 1: return SHIFT_DROP;
		case 2: return SHIFT_SWAP;
		case 3: return DROP;
		case 4: return SWAP;
		case 5: return UP_RCLICK;
		case 6: return DOWN_RCLICK;
		default: return SHIFT_RCLICK;
		}
	}
	
	public static KeyBind getBindFromSlot(int slot) {
		switch (slot) {
		case 9: return SHIFT_RCLICK;
		case 10: return SHIFT_DROP;
		case 11: return SHIFT_SWAP;
		case 12: return DROP;
		case 13: return SWAP;
		case 14: return UP_RCLICK;
		case 15: return DOWN_RCLICK;
		default: return SHIFT_RCLICK;
		}
	}
}
