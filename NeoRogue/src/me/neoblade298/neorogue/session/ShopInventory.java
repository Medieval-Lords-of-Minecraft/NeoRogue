package me.neoblade298.neorogue.session;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class ShopInventory extends CoreInventory {
	private static final int[] SLOT_ORDER = new int[] {0, 2, 4, 6, 8, 9, 11, 13, 15, 17};
	private static final int SELL_PRICE = 10;
	private PlayerSessionData data;
	private static final Component saleCmp = Component.text(" [On Sale]", NamedTextColor.DARK_GREEN, TextDecoration.BOLD, TextDecoration.ITALIC);
	
	public ShopInventory(PlayerSessionData data, ArrayList<Equipment> equips) {
		super(data.getPlayer(), Bukkit.createInventory(data.getPlayer(), 27, Component.text("Shop", NamedTextColor.BLUE)));
		
		this.data = data;
		ItemStack[] contents = inv.getContents();
		
		// Generate 2 random unique sale slots
		HashSet<Integer> saleSlots = new HashSet<Integer>(2);
		while (saleSlots.size() < 2) {
			saleSlots.add(NeoRogue.gen.nextInt(equips.size()));
		}
		
		for (int i = 0; i < equips.size(); i++) {
			Equipment eq = equips.get(i);
			contents[SLOT_ORDER[i]] = setPrice(eq, SLOT_ORDER[i] >= 9, saleSlots.contains(i));
			updateLore(contents[SLOT_ORDER[i]], true);
		}
		contents[22] = CoreInventory.createButton(Material.GOLD_NUGGET, Component.text("You have " + data.getCoins() + " coins", NamedTextColor.YELLOW));
		contents[18] = CoreInventory.createButton(Material.GOLD_NUGGET, Component.text("Sell Items", NamedTextColor.RED),
				(TextComponent) NeoCore.miniMessage().deserialize("Drag equipment here to sell it " +
						"for <yellow>" + SELL_PRICE + " coins</yellow>."), 250, NamedTextColor.GRAY);
		inv.setContents(contents);
	}
	
	private ItemStack setPrice(Equipment eq, boolean expensive, boolean sale) {
		ItemStack item = eq.getItem();
		int price = expensive ? NeoRogue.gen.nextInt(200, 300) : NeoRogue.gen.nextInt(70, 130);
		if (sale) price /= 2;
		NBTItem nbti = new NBTItem(item);
		nbti.setInteger("price", price);
		nbti.setBoolean("sale", sale);
		return nbti.getItem();
	}
	
	private void updateLore(ItemStack item, boolean firstLoad) {
		if (item == null) return;
		NBTItem nbti = new NBTItem(item);
		int price = nbti.getInteger("price");
		ItemMeta meta = item.getItemMeta();
		List<Component> lore = meta.lore();
		NamedTextColor color = data.hasCoins(price) ? NamedTextColor.GREEN : NamedTextColor.RED;
		Component cmp = Component.text(price + " coins", color).decoration(TextDecoration.ITALIC, State.FALSE);
		if (nbti.getBoolean("sale")) {
			cmp = cmp.append(saleCmp);
		}
		
		if (firstLoad) {
			lore.add(0, cmp);
		}
		else {
			lore.set(0, cmp);
		}
		meta.lore(lore);
		item.setItemMeta(meta);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		Inventory iclicked = e.getClickedInventory();
		if (iclicked == null || iclicked.getType() != InventoryType.CHEST) return;
		e.setCancelled(true);
		if (e.getCurrentItem() == null) return;
		int slot = e.getSlot();

		if (slot < 18) {
			int price = slot <= 9 ? 50 : 100;
			if (!data.hasCoins(price)) {
				Util.displayError(p, "You don't have enough coins!");
				return;
			}
			
			data.addCoins(-price);
			p.getInventory().addItem(e.getCurrentItem());
			p.playSound(p, Sound.ENTITY_WANDERING_TRADER_YES, 1F, 1F);
			p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 1F);
			
			ItemStack[] contents = inv.getContents();
			contents[slot] = null;
			for (int i : SLOT_ORDER) {
				updateLore(contents[i], false);
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
