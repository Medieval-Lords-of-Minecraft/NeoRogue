package me.neoblade298.neorogue.session;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.weapons.WoodenSword;
import me.neoblade298.neorogue.player.PlayerSessionData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class ShopItem {
	private static final Component saleCmp = Component
			.text(" [On Sale]", NamedTextColor.DARK_GREEN, TextDecoration.BOLD, TextDecoration.ITALIC);
	private static final ItemStack PURCHASED = CoreInventory
			.createButton(Material.BARRIER, (TextComponent) SharedUtil.color("<red>Purchased!</red>"));
	
	private int price;
	private boolean sale;
	private boolean isPurchased;
	private Equipment eq;

	public ShopItem(Equipment eq, int price, boolean sale) {
		this.eq = eq;
		this.sale = sale;
		this.price = price;
	}
	
	// For deserialization
	public ShopItem(Equipment eq, int price, boolean sale, boolean purchased) {
		this.eq = eq;
		this.sale = sale;
		this.price = price;
		this.isPurchased = purchased;
	}
	
	public ItemStack getItem(PlayerSessionData data, int idx) {
		if (isPurchased) {
			return PURCHASED;
		}
		ItemStack item = eq.getItem();
		update(data, item, true);
		NBTItem nbti = new NBTItem(item);
		nbti.setInteger("idx", idx);
		return nbti.getItem();
	}
	
	public Equipment getEquipment() {
		return eq;
	}
	
	public int getPrice() {
		return price;
	}
	
	public boolean isOnSale() {
		return sale;
	}
	
	public void setPurchased(boolean isPurchased) {
		this.isPurchased = isPurchased;
	}
	
	public void update(PlayerSessionData data, ItemStack item, boolean firstLoad) {
		if (item == null)
			return;
		if (item.getType() == Material.BARRIER)
			return; // Purchased item
		ItemMeta meta = item.getItemMeta();
		List<Component> lore = meta.lore();
		NamedTextColor color = data.hasCoins(price) ? NamedTextColor.GREEN : NamedTextColor.RED;
		Component cmp = Component.text(price + " coins", color).decoration(TextDecoration.ITALIC, State.FALSE);
		if (sale) {
			cmp = cmp.append(saleCmp);
		}
		
		if (firstLoad) {
			lore.add(0, cmp);
		} else {
			lore.set(0, cmp);
		}
		meta.lore(lore);
		item.setItemMeta(meta);
	}
	
	public String serialize() {
		if (isPurchased)
			return "purchased";
		return eq.serialize() + ":" + price + ":" + (sale ? 1 : 0) + ":" + (isPurchased ? 1 : 0);
	}
	
	public boolean isPurchased() {
		return isPurchased;
	}
	
	public static ShopItem deserialize(String str) {
		try {
			String[] split = str.split(":");
			Equipment eq = Equipment.deserialize(split[0]);
			int price = Integer.parseInt(split[1]);
			boolean sale = split[2].equals("1");
			boolean isPurchased = split[3].equals("1");
			return new ShopItem(eq, price, sale, isPurchased);
		}
		catch (Exception e) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to deserialize shop item: " + str);
			e.printStackTrace();
			return new ShopItem(WoodenSword.get(), 100, false, false);
		}
	}

	public String toString() {
		return eq.getId() + "-" + price + "-" + (sale ? "sale" : "nosale");
	}
}
