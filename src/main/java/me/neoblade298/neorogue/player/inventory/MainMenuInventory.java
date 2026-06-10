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
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class MainMenuInventory extends CoreInventory {
	private static final int NEW_GAME = 11, LOAD_GAME = 12, ACHIEVEMENTS = 13, STATS = 14, UNLOCKS = 15;

	public MainMenuInventory(Player p) {
		super(p, Bukkit.createInventory(p, 27, Component.text("NeoRogue", NamedTextColor.DARK_RED)));
		setupInventory();
	}

	private void setupInventory() {
		p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
		ItemStack[] contents = inv.getContents();
		contents[NEW_GAME] = CoreInventory.createButton(Material.EMERALD,
				Component.text("New Game", NamedTextColor.GREEN));
		contents[LOAD_GAME] = CoreInventory.createButton(Material.WRITABLE_BOOK,
				Component.text("Load Game", NamedTextColor.GOLD));
		contents[ACHIEVEMENTS] = CoreInventory.createButton(Material.DIAMOND,
				Component.text("Achievements", NamedTextColor.AQUA));
		contents[STATS] = CoreInventory.createButton(Material.PAPER,
				Component.text("Stats", NamedTextColor.YELLOW));
		contents[UNLOCKS] = CoreInventory.createButton(Material.ENDER_EYE,
				Component.text("Unlocks", NamedTextColor.LIGHT_PURPLE));
		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getClickedInventory() == null || e.getClickedInventory().getType() != InventoryType.CHEST) return;
		if (e.getCurrentItem() == null) return;

		PlayerData pd = PlayerManager.getPlayerData(p.getUniqueId());
		if (pd == null) return;

		switch (e.getSlot()) {
		case NEW_GAME:
			new NewGameSlotInventory(p, pd);
			break;
		case LOAD_GAME:
			new LoadGameSlotInventory(p, pd);
			break;
		case ACHIEVEMENTS:
			p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 1F);
			break;
		case STATS:
			p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 1F);
			break;
		case UNLOCKS:
			new UnlocksMenuInventory(p);
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
