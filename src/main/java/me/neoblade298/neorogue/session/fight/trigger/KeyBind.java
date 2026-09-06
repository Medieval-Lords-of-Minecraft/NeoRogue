package me.neoblade298.neorogue.session.fight.trigger;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public enum KeyBind {
	
	SHIFT_RCLICK(27, 0, "<key:key.sneak>+<key:key.use>", Trigger.SHIFT_RCLICK),
	SHIFT_DROP(28, 1, "<key:key.sneak>+<key:key.drop>", Trigger.SHIFT_DROP),
	SHIFT_SWAP(29, 2, "<key:key.sneak>+<key:key.swapOffhand>", Trigger.SHIFT_SWAP),
	DROP(30, 3, "<key:key.drop>", Trigger.DROP),
	SWAP(31, 4, "<key:key.swapOffhand>", Trigger.SWAP),
	UP_RCLICK(32, 5, "Look up+<key:key.use>", Trigger.UP_RCLICK),
	DOWN_RCLICK(33, 6, "Look down+<key:key.use>", Trigger.DOWN_RCLICK);

	private int invSlot, dataSlot;
	private ItemStack item;
	private Component display;
	private Trigger trigger;
	private KeyBind(int invSlot, int dataSlot, String display, Trigger trigger) {
		this.invSlot = invSlot;
		this.dataSlot = dataSlot;
		this.display = NeoCore.miniMessage().deserialize(display).decoration(TextDecoration.ITALIC, State.FALSE);
		this.item = CoreInventory.createButton(Material.BLUE_STAINED_GLASS_PANE,
				Component.text("Ability Slot", NamedTextColor.BLUE),
				Component.text("Bound to ", NamedTextColor.YELLOW).append(this.display),
				Component.text("Drag a weapon, ability, or consumable", NamedTextColor.GRAY),
				Component.text("here to bind it!", NamedTextColor.GRAY));
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

	public static boolean isKeybindSlot(int slot) {
		return slot >= 9 && slot <= 15;
	}
}
