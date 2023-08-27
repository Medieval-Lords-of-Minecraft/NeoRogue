package me.neoblade298.neorogue.session.chance;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;

public class ChanceInventory extends CoreInventory {
	private ChanceInstance inst;
	private ChanceSet set;
	private int stage = 0;

	public ChanceInventory(Player p, ChanceInstance inst, ChanceSet set) {
		super(p, Bukkit.createInventory(p, 18, "§9Chance Event"));
		this.set = set;
		this.inst = inst;
		setupInventory();
	}
	
	private void setupInventory() {
		// Create title
		ItemStack[] contents = inv.getContents();
		ItemStack title = CoreInventory.createButton(set.getMaterial(), set.getDisplay());
		ItemMeta meta = title.getItemMeta();
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("§7Stage §c" + (stage + 1));
		lore.addAll(set.stages.get(stage).description);
		meta.setLore(lore);
		title.setItemMeta(meta);
		contents[4] = title;
		
		// Setup choices
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		Inventory inv = e.getClickedInventory();
		if (inv == null || inv.getType() != InventoryType.CHEST) return;
		
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
		
	}

}
