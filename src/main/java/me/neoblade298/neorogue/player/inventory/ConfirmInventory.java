package me.neoblade298.neorogue.player.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

// Generic yes/no confirmation screen. Provide a title, an optional display item describing the
// action, and callbacks for confirm/cancel (typically reopening the previous inventory).
public class ConfirmInventory extends CoreInventory {
	private static final int CONFIRM = 11, DISPLAY = 13, CANCEL = 15;
	private final Runnable onConfirm, onCancel;

	public ConfirmInventory(Player p, Component title, @Nullable ItemStack display, Runnable onConfirm, Runnable onCancel) {
		super(p, Bukkit.createInventory(p, 27, title));
		this.onConfirm = onConfirm;
		this.onCancel = onCancel;
		p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);

		ItemStack[] contents = inv.getContents();
		contents[CONFIRM] = CoreInventory.createButton(Material.GREEN_WOOL, Component.text("Confirm", NamedTextColor.GREEN));
		contents[CANCEL] = CoreInventory.createButton(Material.RED_WOOL, Component.text("Cancel", NamedTextColor.RED));
		if (display != null) contents[DISPLAY] = display;
		for (int i = 0; i < contents.length; i++) {
			if (contents[i] == null) contents[i] = CoreInventory.createButton(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
		}
		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getClickedInventory() == null || e.getClickedInventory().getType() != InventoryType.CHEST) return;
		int slot = e.getSlot();
		if (slot == CONFIRM) {
			onConfirm.run();
		}
		else if (slot == CANCEL) {
			onCancel.run();
		}
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}
}
