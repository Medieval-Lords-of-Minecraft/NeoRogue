package me.neoblade298.neorogue.player;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBTItem;
import me.ascheladd.asheconomy.pricing.MaterialPrices;

// A player's persistent stash of sellable vanilla items plus its two limits. Owned by PlayerData
// and persisted to SQL (items in neorogue_playercargo, limits in neorogue_playercargo_meta).
public class Cargo {
	// LinkedHashMap keeps a stable slot ordering in the GUI as items are added.
	private final LinkedHashMap<Material, Integer> items = new LinkedHashMap<Material, Integer>();
	private int capacity; // Max total item count across all materials
	private int slots;     // Max number of unique materials

	public Cargo(int capacity, int slots) {
		this.capacity = capacity;
		this.slots = slots;
	}

	// An item may be deposited only if its material has a mapped price and it carries no special
	// metadata: no display name, lore, enchantments, damage, or plugin NBT (equipId).
	public static boolean isEligible(ItemStack item) {
		if (item == null || item.getType().isAir()) return false;
		if (!MaterialPrices.hasPrice(item.getType())) return false;
		if (new NBTItem(item).hasTag("equipId")) return false;
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return true;
		if (meta.hasDisplayName() || meta.hasLore() || meta.hasEnchants()) return false;
		if (meta instanceof Damageable && ((Damageable) meta).hasDamage()) return false;
		return true;
	}

	public Map<Material, Integer> getItems() {
		return items;
	}

	public int getCount(Material mat) {
		return items.getOrDefault(mat, 0);
	}

	public int getTotalItems() {
		int total = 0;
		for (int amt : items.values()) total += amt;
		return total;
	}

	public int getUsedSlots() {
		return items.size();
	}

	// Adds up to the requested amount, respecting the slot and capacity limits.
	// Returns the amount actually added.
	public int addItem(Material mat, int amount) {
		if (mat == null || amount <= 0) return 0;
		boolean isNew = !items.containsKey(mat);
		if (isNew && getUsedSlots() >= slots) return 0;
		int space = capacity - getTotalItems();
		if (space <= 0) return 0;
		int toAdd = Math.min(amount, space);
		items.merge(mat, toAdd, Integer::sum);
		return toAdd;
	}

	// Removes up to the requested amount. Returns the amount actually removed.
	public int removeItem(Material mat, int amount) {
		Integer current = items.get(mat);
		if (current == null || amount <= 0) return 0;
		int toRemove = Math.min(amount, current);
		if (toRemove >= current) items.remove(mat);
		else items.put(mat, current - toRemove);
		return toRemove;
	}

	// Loads an item directly from storage, bypassing limit checks.
	public void load(Material mat, int amount) {
		if (mat == null || amount <= 0) return;
		items.merge(mat, amount, Integer::sum);
	}

	// Removes all items (limits are untouched).
	public void clear() {
		items.clear();
	}

	public double getSellValue(Material mat) {
		return MaterialPrices.getPrice(mat) * getCount(mat);
	}

	public double getTotalSellValue() {
		double total = 0;
		for (Map.Entry<Material, Integer> ent : items.entrySet()) {
			total += MaterialPrices.getPrice(ent.getKey()) * ent.getValue();
		}
		return total;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public void addCapacity(int amount) {
		this.capacity += amount;
	}

	public int getSlots() {
		return slots;
	}

	public void setSlots(int slots) {
		this.slots = slots;
	}

	public void addSlots(int amount) {
		this.slots += amount;
	}
}
