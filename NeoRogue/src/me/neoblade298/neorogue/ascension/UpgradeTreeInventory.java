package me.neoblade298.neorogue.ascension;

import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.player.PlayerData;

public class UpgradeTreeInventory extends CoreInventory {
	private PlayerData data;
	private UpgradeTree tree;

	public UpgradeTreeInventory(Player p, UpgradeTree tree, PlayerData data) {
		super(p, Bukkit.createInventory(p, 54, "§9Ascension Tree: " + tree.getDisplay()));
		this.data = data;
		this.tree = tree;
		ItemStack[] contents = inv.getContents();
		
		for (Entry<Integer, UpgradeHolder> ent : tree.getUpgrades().entrySet()) {
			contents[ent.getKey()] = ent.getValue().getIcon(data);
		}
		contents[53] = CoreInventory.createButton(Material.ENCHANTED_BOOK, "&7You have &e" + data.getPoints() + " &7points to use");
		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		ItemStack item = e.getCurrentItem();
		int slot = e.getRawSlot();
		
		if (item != null && tree.getUpgrades().containsKey(slot)) {
			if (data.getPoints() <= 0) return;
			tree.getUpgrades().get(slot).onClick(p, data);
			
			ItemStack[] contents = inv.getContents();
			for (Entry<Integer, UpgradeHolder> ent : tree.getUpgrades().entrySet()) {
				contents[ent.getKey()] = ent.getValue().updateItem(contents[ent.getKey()], data);
			}
			contents[53] = CoreInventory.createButton(Material.ENCHANTED_BOOK, "&7You have &e" + data.getPoints() + " &7points to use");
			inv.setContents(contents);
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
