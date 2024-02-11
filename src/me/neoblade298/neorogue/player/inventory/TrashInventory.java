package me.neoblade298.neorogue.player.inventory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Equipment;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class TrashInventory extends CoreInventory {

	public TrashInventory(Player p) {
		super(p, Bukkit.createInventory(p, 27, Component.text("Trash Can", NamedTextColor.RED)));
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		if (e.getClickedInventory() == null) return;
		if (e.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD ||
				e.getAction() == InventoryAction.HOTBAR_SWAP) {
			e.setCancelled(true);
			return;
		}
		
		if (e.getClickedInventory().getType() != InventoryType.CHEST) {
			if (e.getCurrentItem() == null) return;
			ItemStack item = e.getCurrentItem();
			NBTItem nbti = new NBTItem(item);
			// Only allow picking up equipment
			if (!nbti.getKeys().contains("equipId")) {
				e.setCancelled(true);
			}
			
			Equipment eq = Equipment.get(nbti.getString("equipId"), false);
			if (eq.isCursed()) {
				Util.displayError(p, "You can't dispose of curses except in a shop!");
				e.setCancelled(true);
			}
			return;
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
