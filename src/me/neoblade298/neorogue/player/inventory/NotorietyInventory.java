package me.neoblade298.neorogue.player.inventory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.session.Session;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class NotorietyInventory extends CoreInventory {
	private Session s;

	public NotorietyInventory(Player p, Session s) {
		super(p, Bukkit.createInventory(p, 9, Component.text("Notoriety Settings", NamedTextColor.BLUE)));
		this.s = s;
		setupInventory();
	}
	
	private void setupInventory() {
		ItemStack[] contents = inv.getContents();
		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {

	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}
}
