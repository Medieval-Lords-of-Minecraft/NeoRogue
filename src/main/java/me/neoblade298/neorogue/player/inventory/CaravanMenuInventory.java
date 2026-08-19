package me.neoblade298.neorogue.player.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.api.NeoRogueAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CaravanMenuInventory extends CoreInventory {
	private static final int UPGRADE_CARAVAN = 11;
	private static final int MANAGE_CARGO = 15;

	public CaravanMenuInventory(Player player) {
		super(player, Bukkit.createInventory(player, 27, Component.text("Caravan", NamedTextColor.DARK_AQUA)));
		ItemStack[] contents = inv.getContents();
		contents[UPGRADE_CARAVAN] = createButton(Material.ANVIL,
				Component.text("Upgrade Caravan", NamedTextColor.AQUA));
		contents[MANAGE_CARGO] = createButton(Material.CHEST_MINECART,
				Component.text("Manage Cargo", NamedTextColor.GOLD));
		inv.setContents(contents);
		player.playSound(player, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent event) {
		event.setCancelled(true);
		if (event.getClickedInventory() != inv) return;
		if (event.getSlot() == UPGRADE_CARAVAN) {
			NeoRogueAPI.openCaravanUpgradeMenu(p);
		} else if (event.getSlot() == MANAGE_CARGO) {
			NeoRogueAPI.openCargoMenu(p);
		}
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent event) {
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent event) {
		event.setCancelled(true);
	}
}