package me.neoblade298.neorogue.session;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;

public class CampfireChoiceInventory extends CoreInventory {
	private CampfireInstance inst;

	public CampfireChoiceInventory(Player p, CampfireInstance inst) {
		super(p, Bukkit.createInventory(p, 9, "§9Choose"));
		this.inst = inst;
		ItemStack[] contents = inv.getContents();
		for (int i = 0; i < 4; i++) {
			contents[i] = CoreInventory.createButton(Material.SOUL_LANTERN, "&aRest", 
					"&7Everyone in the party heals for", "&725% of their max health.");
			contents[5 + i] = CoreInventory.createButton(Material.ANVIL, "&6Upgrade", 
					"&7Everyone in the party gets to", "&7upgrade 1 equipment.");
		}
		contents[4] = CoreInventory.createButton(Material.GRAY_STAINED_GLASS_PANE, " ");
		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		Inventory iclicked = e.getClickedInventory();
		if (iclicked == null || iclicked.getType() != InventoryType.CHEST) return;
		e.setCancelled(true);
		
		int slot = e.getSlot();
		if (slot < 4) {
			inst.chooseState(true);
			p.closeInventory();
		}
		else if (slot > 4) {
			inst.chooseState(false);
			p.closeInventory();
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