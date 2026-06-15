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
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class AchievementsMenuInventory extends CoreInventory {
	private static final int BACK = 10, GLOBAL = 11, WARRIOR = 13, THIEF = 14, ARCHER = 15, MAGE = 16;

	public AchievementsMenuInventory(Player p) {
		super(p, Bukkit.createInventory(p, 27, Component.text("Achievements", NamedTextColor.AQUA)));
		setupInventory();
	}

	private void setupInventory() {
		p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
		ItemStack[] contents = inv.getContents();
		contents[WARRIOR] = CoreInventory.createButton(Material.IRON_SWORD,
				Component.text("Warrior", NamedTextColor.RED));
		contents[THIEF] = CoreInventory.createButton(Material.IRON_INGOT,
				Component.text("Thief", NamedTextColor.YELLOW));
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

		PlayerData pd = PlayerManager.getPlayerData(p.getUniqueId());
		if (pd == null) return;

		switch (e.getSlot()) {
		case WARRIOR:
			new AchievementsInventory(p, pd, EquipmentClass.WARRIOR);
			break;
		case THIEF:
			new AchievementsInventory(p, pd, EquipmentClass.THIEF);
			break;
		case ARCHER:
			new AchievementsInventory(p, pd, EquipmentClass.ARCHER);
			break;
		case MAGE:
			new AchievementsInventory(p, pd, EquipmentClass.MAGE);
			break;
		case GLOBAL:
			new AchievementsInventory(p, pd, null);
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
