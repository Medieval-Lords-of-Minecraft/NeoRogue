package me.neoblade298.neorogue.session;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class ShopInventory extends CoreInventory {
	public static final int[] SLOT_ORDER = new int[] {0, 2, 4, 6, 8, 9, 11, 13, 15, 17};
	private static final int SELL_PRICE = 10;
	private PlayerSessionData data;
	private ArrayList<ShopItem> shopItems;
	
	public ShopInventory(PlayerSessionData data, ArrayList<ShopItem> items) {
		super(data.getPlayer(), Bukkit.createInventory(data.getPlayer(), 27, Component.text("Shop", NamedTextColor.BLUE)));
		
		this.data = data;
		this.shopItems = items;
		ItemStack[] contents = inv.getContents();
		
		
		for (int i = 0; i < items.size(); i++) {
			contents[SLOT_ORDER[i]] = items.get(i).getItem(data);
		}
		contents[22] = CoreInventory.createButton(Material.GOLD_NUGGET, Component.text("You have " + data.getCoins() + " coins", NamedTextColor.YELLOW));
		contents[18] = CoreInventory.createButton(Material.GOLD_NUGGET, Component.text("Sell Items", NamedTextColor.RED),
				(TextComponent) NeoCore.miniMessage().deserialize("Drag equipment here to sell it " +
						"for <yellow>" + SELL_PRICE + " coins</yellow>."), 250, NamedTextColor.GRAY);
		inv.setContents(contents);
	}
	
	

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		Inventory iclicked = e.getClickedInventory();
		if (iclicked == null || iclicked.getType() != InventoryType.CHEST) return;
		e.setCancelled(true);
		if (e.getCurrentItem() == null) return; // Must have clicked on an item in the inv
		if (e.getCurrentItem().getType() == Material.BARRIER) {
			Util.displayError(p, "You've already purchased this item!");
			return;
		}
		int slot = e.getSlot();

		if (slot < 18) {
			ShopItem shopItem = null;
			for (int i = 0 ; i < SLOT_ORDER.length; i++) {
				if (SLOT_ORDER[i] == slot) {
					shopItem = shopItems.get(i);
					break;
				}
			}
			int price = shopItem.getPrice();
			
			if (e.isRightClick()) {
				new GlossaryInventory(p, shopItem.getEquipment(), this);
				return;
			}
			
			if (!data.hasCoins(price)) {
				Util.displayError(p, "You don't have enough coins! You need " + (price - data.getCoins()) + " more.");
				return;
			}
			shopItem.setPurchased(true);
			
			data.addCoins(-price);
			p.getInventory().addItem(e.getCurrentItem());
			p.playSound(p, Sound.ENTITY_WANDERING_TRADER_YES, 1F, 1F);
			p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 1F);
			
			ItemStack[] contents = inv.getContents();
			contents[slot] = shopItem.getItem(data);
			for (ShopItem si : shopItems) {
				si.updateLore(data, contents[si.getSlot()], false);
			}
			contents[22] = CoreInventory.createButton(Material.GOLD_NUGGET, Component.text("You have " + data.getCoins() + " coins", NamedTextColor.YELLOW));
			inv.setContents(contents);
		}
		else {
			if (slot == 18 && e.getCursor() != null) {
				data.addCoins(SELL_PRICE);
				inv.setItem(22, CoreInventory.createButton(Material.GOLD_NUGGET,
						Component.text("You have " + data.getCoins() + " coins", NamedTextColor.YELLOW)));
				p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 1F);
				p.setItemOnCursor(null);
			}
		}
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		p.playSound(p, Sound.BLOCK_CHEST_CLOSE, 1F, 1F);
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}
}
