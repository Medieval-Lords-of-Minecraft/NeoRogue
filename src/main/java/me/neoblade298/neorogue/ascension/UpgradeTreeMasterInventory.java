package me.neoblade298.neorogue.ascension;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.player.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class UpgradeTreeMasterInventory extends CoreInventory  {
	private PlayerData data;

	public UpgradeTreeMasterInventory(Player p, PlayerData data) {
		super(p, Bukkit.createInventory(p, 54, Component.text("Ascension Tree Branches", NamedTextColor.BLUE)));
		this.data = data;
		
		ItemStack[] contents = inv.getContents();
		for (UpgradeTree tree : UpgradeTree.getTrees().values()) {
			contents[tree.getSlot()] = tree.getIcon(data);
		}
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		ItemStack item = e.getCurrentItem();
		
		if (item != null && item.getType() != Material.BARRIER) {
			UpgradeTree tree = UpgradeTree.get(new NBTItem(item).getString("id"));
			new UpgradeTreeInventory(p, tree, data);
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
