package me.neoblade298.neorogue.player.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.listeners.InventoryListener;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class TrashInventory extends CoreInventory implements ShiftClickableInventory {
	public TrashInventory(PlayerSessionData data) {
		super(data.getPlayer(), Bukkit.createInventory(data.getPlayer(), 9, Component.text("Trash", NamedTextColor.RED)));
		new PlayerSessionInventory(data);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		PlayerSessionInventory pinv = (PlayerSessionInventory) InventoryListener.getLowerInventory(p);
		if (e.isShiftClick()) {
			if (e.getCurrentItem() == null) return;
			e.setCancelled(true);
			if (!pinv.canShiftClickIn(inv.getItem(0))) return;
			pinv.handleShiftClickIn(inv.getItem(0));
			e.setCurrentItem(null);
			p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
		}
		else {
			if (!e.getCursor().getType().isAir() || e.getCurrentItem() != null) 
				p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
			if (!e.getCursor().getType().isAir()) {
				pinv.clearHighlights();
			}
			if (e.getCurrentItem() != null) {
				NBTItem nbti = new NBTItem(e.getCurrentItem());
				Equipment eq = Equipment.get(nbti.getString("equipId"), false);
				pinv.setHighlights(eq.getType());
			}
		}
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}

	@Override
	public boolean canShiftClickIn(ItemStack item) {
		return inv.firstEmpty() != -1;
	}

	@Override
	public void handleShiftClickIn(ItemStack item) {
		inv.addItem(item);
	}
}
