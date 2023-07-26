package me.neoblade298.neorogue.player;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.equipment.Equipment;

public class ReforgeOptionsInventory extends CoreInventory {
	private int slot;
	private boolean isEquipSlot;
	private Equipment toReforge;
	private PlayerSessionInventory prev;
	public ReforgeOptionsInventory(PlayerSessionInventory prev, int slot, boolean isEquipSlot, Equipment toReforge) {
		super(prev.getPlayer(), Bukkit.createInventory(prev.getPlayer(), 9, "§9Reforge Options"));

		ItemStack[] contents = inv.getContents();
		
		for (int i = 0; i < 9; i++) {
			contents[i] = CoreInventory.createButton(Material.RED_STAINED_GLASS_PANE, "&cCancel");
		}
		
		ArrayList<String> options = toReforge.getReforgeOptions();
		int offset = options.size() - 1;
		for (int i = 0; i < options.size(); i++) {
			contents[(2 * i) - offset] = Equipment.getEquipment(options.get(i), false).getItem();
		}
	}
	
	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getClickedInventory().getType() != InventoryType.CHEST) return;
		if (e.getCurrentItem().getType() == Material.RED_STAINED_GLASS_PANE) {
			p.closeInventory();
		}
	}
	
	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		prev.openInventory();
	}
	
	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		// TODO Auto-generated method stub
		
	}
}
