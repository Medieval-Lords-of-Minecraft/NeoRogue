package me.neoblade298.neorogue.session;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class ShopItem {
	private static final Component saleCmp = Component.text(" [On Sale]", NamedTextColor.DARK_GREEN, TextDecoration.BOLD, TextDecoration.ITALIC);
	
	private int price, slot;
	private boolean sale;
	private Equipment eq;
	public ShopItem(Equipment eq, int slot, boolean expensive, boolean sale) {
		this.eq = eq;
		this.sale = sale;
		this.slot = slot;
		price = expensive ? NeoRogue.gen.nextInt(200, 300) : NeoRogue.gen.nextInt(70, 130);
		if (sale) price /= 2;
	}
	
	// For deserialization
	public ShopItem(Equipment eq, int price, int slot, boolean sale) {
		this.eq = eq;
		this.sale = sale;
		this.slot = slot;
		this.price = price;
	}
	
	public ItemStack getItem(PlayerSessionData data) {
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
	
	public void updateLore(PlayerSessionData data, ItemStack item, boolean firstLoad) {
		if (item == null) return;
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
		return eq.serialize() + ":" + price + ":" + slot + ":" + (sale ? 1 : 0);
	}
	
	public static ShopItem deserialize(String str) {
		String[] split = str.split(":");
		Equipment eq = Equipment.deserialize(split[0]);
		int price = Integer.parseInt(split[1]);
		int slot = Integer.parseInt(split[2]);
		boolean sale = split[3].equals("1");
		return new ShopItem(eq, price, slot, sale);
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
