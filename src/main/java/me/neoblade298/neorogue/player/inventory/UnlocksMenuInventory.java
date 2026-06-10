package me.neoblade298.neorogue.player.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class UnlocksMenuInventory extends CoreInventory {
	private static final int WARRIOR = 10, THIEF = 11, ARCHER = 12, MAGE = 13, GLOBAL = 15, BACK = 16;

	public UnlocksMenuInventory(Player p) {
		super(p, Bukkit.createInventory(p, 27, Component.text("Unlocks", NamedTextColor.LIGHT_PURPLE)));
		setupInventory();
	}

	private void setupInventory() {
		p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
		ItemStack[] contents = inv.getContents();
		contents[WARRIOR] = CoreInventory.createButton(Material.IRON_SWORD,
				Component.text("Warrior", NamedTextColor.RED));
		contents[THIEF] = CoreInventory.createButton(Material.IRON_INGOT,
				Component.text("Thief", NamedTextColor.DARK_PURPLE));
		contents[ARCHER] = CoreInventory.createButton(Material.BOW,
				Component.text("Archer", NamedTextColor.GREEN));
		contents[MAGE] = CoreInventory.createButton(Material.BLAZE_ROD,
				Component.text("Mage", NamedTextColor.BLUE));
		contents[GLOBAL] = CoreInventory.createButton(Material.NETHER_STAR,
				Component.text("Global", NamedTextColor.GOLD));
		contents[BACK] = CoreInventory.createButton(Material.BARRIER,
				Component.text("Back", NamedTextColor.RED));
		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getClickedInventory() == null || e.getClickedInventory().getType() != InventoryType.CHEST) return;
		if (e.getCurrentItem() == null) return;

		switch (e.getSlot()) {
		case WARRIOR:
		case THIEF:
		case ARCHER:
		case MAGE:
		case GLOBAL:
			p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 1F);
			break;
		case BACK:
			new MainMenuInventory(p);
			break;
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
