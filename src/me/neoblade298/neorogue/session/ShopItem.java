package me.neoblade298.neorogue.session;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class ShopItem {
	private static final Component saleCmp = Component.text(" [On Sale]", NamedTextColor.DARK_GREEN, TextDecoration.BOLD, TextDecoration.ITALIC);
	private static final ItemStack PURCHASED = CoreInventory.createButton(Material.BARRIER, (TextComponent) SharedUtil.color("<red>Purchased!</red>"));
	
	private int price, slot;
	private boolean sale;
	private boolean isPurchased;
	private Equipment eq;
	public ShopItem(Equipment eq, int slot, boolean expensive, boolean sale) {
		this.eq = eq;
		this.sale = sale;
		this.slot = slot;
		price = expensive ? NeoRogue.gen.nextInt(100, 180) : NeoRogue.gen.nextInt(70, 130);
		if (sale) price /= 2;
	}
	
	// For deserialization
	public ShopItem(Equipment eq, int price, int slot, boolean sale, boolean purchased) {
		this.eq = eq;
		this.sale = sale;
		this.slot = slot;
		this.price = price;
	}
	
	public ItemStack getItem(PlayerSessionData data) {
		if (isPurchased) {
			return PURCHASED;
		}
		ItemStack item = eq.getItem();
		updateLore(data, item, true);
		return item;
	}
	
	public Equipment getEquipment() {
		return eq;
	}
	
	public int getPrice() {
		return price;
	}
	
	public int getSlot() {
		return slot;
	}
	
	public boolean isOnSale() {
		return sale;
	}
	
	public void setPurchased(boolean isPurchased) {
		this.isPurchased = isPurchased;
	}
	
	public void updateLore(PlayerSessionData data, ItemStack item, boolean firstLoad) {
		if (item == null) return;
		if (item.getType() == Material.BARRIER) return; // Purchased item
		ItemMeta meta = item.getItemMeta();
		List<Component> lore = meta.lore();
		NamedTextColor color = data.hasCoins(price) ? NamedTextColor.GREEN : NamedTextColor.RED;
		Component cmp = Component.text(price + " coins", color).decoration(TextDecoration.ITALIC, State.FALSE);
		if (sale) {
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
	
	public String serialize() {
		if (isPurchased) return "purchased";
		return eq.serialize() + ":" + price + ":" + slot + ":" + (sale ? 1 : 0) + ":" + (isPurchased ? 1 : 0);
	}
	
	public boolean isPurchased() {
		return isPurchased;
	}
	
	public static ShopItem deserialize(String str) {
		String[] split = str.split(":");
		Equipment eq = Equipment.deserialize(split[0]);
		int price = Integer.parseInt(split[1]);
		int slot = Integer.parseInt(split[2]);
		boolean sale = split[3].equals("1");
		boolean isPurchased = split[4].equals("1");
		return new ShopItem(eq, price, slot, sale, isPurchased);
	}
	
	public static String serializeShopItems(ArrayList<ShopItem> items) {
		String serialized = "";
		boolean first = true;
		for (ShopItem item : items) {
			if (first) {
				first = false;
				serialized += item.serialize();
				continue;
			}
			serialized += "," + item.serialize();
		}
		return serialized;
	}
	
	public static ArrayList<ShopItem> deserializeShopItems(String str) {
		String[] split = str.split(",");
		ArrayList<ShopItem> arr = new ArrayList<ShopItem>(split.length);
		for (int i = 0; i < split.length; i++) {
			arr.add(deserialize(split[i]));
		}
		return arr;
	}
}
