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
import org.jetbrains.annotations.Nullable;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.listeners.InventoryListener;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.PlayerSessionInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ShrineChoiceInventory extends CoreInventory {
	private ShrineInstance inst;

	public ShrineChoiceInventory(Player p, @Nullable PlayerSessionData data, ShrineInstance inst) {
		super(p, Bukkit.createInventory(p, 9, Component.text("Choose", NamedTextColor.BLUE)));
		this.inst = inst;
		if (data != null) InventoryListener.registerPlayerInventory(p, new PlayerSessionInventory(data));
		ItemStack[] contents = inv.getContents();
		for (int i = 0; i < 4; i++) {
			contents[i] = CoreInventory.createButton(Material.SOUL_LANTERN, Component.text("Rest", NamedTextColor.GREEN), 
					"Everyone in the party heals for 35% of their max health.", 250, NamedTextColor.GRAY);
			contents[5 + i] = CoreInventory.createButton(Material.ANVIL, Component.text("Upgrade", NamedTextColor.GOLD), 
					"Everyone in the party gets to upgrade 1 equipment.", 250, NamedTextColor.GRAY);
		}
		contents[4] = CoreInventory.createButton(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
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
