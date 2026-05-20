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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class UpgradeTreeInventory extends CoreInventory {
	private PlayerData data;
	private UpgradeTree tree;

	public UpgradeTreeInventory(Player p, UpgradeTree tree, PlayerData data) {
		super(p, Bukkit.createInventory(p, 54, Component.text("Ascension Tree: " + tree.getDisplay(), NamedTextColor.BLUE)));
		this.data = data;
		this.tree = tree;
		ItemStack[] contents = inv.getContents();
		
		for (Entry<Integer, UpgradeHolder> ent : tree.getUpgrades().entrySet()) {
			contents[ent.getKey()] = ent.getValue().getIcon(data);
		}
		
		TextComponent c = Component.text("You have ", NamedTextColor.GRAY)
				.append(Component.text(data.getPoints(), NamedTextColor.YELLOW))
				.append(Component.text(" points to use"));
		contents[53] = CoreInventory.createButton(Material.ENCHANTED_BOOK, c);
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
			TextComponent c = Component.text("You have ", NamedTextColor.GRAY)
					.append(Component.text(data.getPoints(), NamedTextColor.YELLOW))
					.append(Component.text(" points to use"));
			contents[53] = CoreInventory.createButton(Material.ENCHANTED_BOOK, c);
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
