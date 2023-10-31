package me.neoblade298.neorogue.player;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public enum KeyBind {
	
	SHIFT_RCLICK(9, 0, Component.text("Shift+RClick"), Trigger.SHIFT_RCLICK,
			CoreInventory.createButton(Material.BLUE_STAINED_GLASS_PANE,
			Component.text("Ability Slot", NamedTextColor.BLUE),
			Component.text("Bound to Shift+RClick", NamedTextColor.YELLOW),
			Component.text("Drag an ability here to bind it!", NamedTextColor.GRAY))),
	SHIFT_DROP(10, 1, Component.text("Shift+Drop"), Trigger.SHIFT_DROP,
			CoreInventory.createButton(Material.BLUE_STAINED_GLASS_PANE,
			Component.text("Ability Slot", NamedTextColor.BLUE),
			Component.text("Bound to Shift+Drop", NamedTextColor.YELLOW),
			Component.text("Drag an ability here to bind it!", NamedTextColor.GRAY))),
	SHIFT_SWAP(11, 2, Component.text("Shift+Swap"), Trigger.SHIFT_SWAP,
			CoreInventory.createButton(Material.BLUE_STAINED_GLASS_PANE,
			Component.text("Ability Slot", NamedTextColor.BLUE),
			Component.text("Bound to Shift+Swap", NamedTextColor.YELLOW),
			Component.text("Drag an ability here to bind it!", NamedTextColor.GRAY))),
	DROP(12, 3, Component.text("Drop"), Trigger.DROP,
			CoreInventory.createButton(Material.BLUE_STAINED_GLASS_PANE,
			Component.text("Ability Slot", NamedTextColor.BLUE),
			Component.text("Bound to Drop", NamedTextColor.YELLOW),
			Component.text("Drag an ability here to bind it!", NamedTextColor.GRAY))),
	SWAP(13, 4, Component.text("Swap"), Trigger.SWAP,
			CoreInventory.createButton(Material.BLUE_STAINED_GLASS_PANE,
			Component.text("Ability Slot", NamedTextColor.BLUE),
			Component.text("Bound to Swap", NamedTextColor.YELLOW),
			Component.text("Drag an ability here to bind it!", NamedTextColor.GRAY))),
	UP_RCLICK(14, 5, Component.text("Look up+RClick"), Trigger.UP_RCLICK,
			CoreInventory.createButton(Material.BLUE_STAINED_GLASS_PANE,
			Component.text("Ability Slot", NamedTextColor.BLUE),
			Component.text("Bound to Look up+RClick", NamedTextColor.YELLOW),
			Component.text("Drag an ability here to bind it!", NamedTextColor.GRAY))),
	DOWN_RCLICK(15, 6, Component.text("Look down+RClick"), Trigger.DOWN_RCLICK,
			CoreInventory.createButton(Material.BLUE_STAINED_GLASS_PANE,
			Component.text("Ability Slot", NamedTextColor.BLUE),
			Component.text("Bound to Look down+RClick", NamedTextColor.YELLOW),
			Component.text("Drag an ability here to bind it!", NamedTextColor.GRAY)));

	private int invSlot, dataSlot;
	private ItemStack item;
	private Component display;
	private Trigger trigger;
	private KeyBind(int invSlot, int dataSlot, Component display, Trigger trigger, ItemStack item) {
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
	
	public Component getDisplay() {
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
