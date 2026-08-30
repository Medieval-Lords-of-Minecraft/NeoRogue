package me.neoblade298.neorogue.player;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

// A player's persistent stash of sellable vanilla items plus its two limits. Owned by PlayerData
// and persisted to SQL (items in neorogue_playercargo). The limits are derived from the player's
// purchased caravan upgrades on login (see PlayerData.recomputeCaravanState), not persisted directly.
public class Cargo {
	// LinkedHashMap keeps a stable slot ordering in the GUI as items are added.
	private final LinkedHashMap<CargoItem, Integer> items = new LinkedHashMap<CargoItem, Integer>();
	private int capacity; // Max total item count across all variants
	private int slots;     // Max number of unique item variants

	public Cargo(int capacity, int slots) {
		this.capacity = capacity;
		this.slots = slots;
	}

	// Eligibility is defined by CargoItem's strict metadata whitelist and AshEconomy's exact quote.
	public static boolean isEligible(ItemStack item) {
		return CargoItem.fromItem(item).isPresent();
	}

	public Map<CargoItem, Integer> getItems() {
		return items;
	}

	public int getCount(CargoItem item) {
		return items.getOrDefault(item, 0);
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
	public int addItem(CargoItem item, int amount) {
		if (item == null || amount <= 0) return 0;
		boolean isNew = !items.containsKey(item);
		if (isNew && getUsedSlots() >= slots) return 0;
		int space = capacity - getTotalItems();
		if (space <= 0) return 0;
		int toAdd = Math.min(amount, space);
		items.merge(item, toAdd, Integer::sum);
		return toAdd;
	}

	public int addItem(ItemStack stack, int amount) {
		return CargoItem.fromItem(stack).map(item -> addItem(item, amount)).orElse(0);
	}

	// Convenience for ordinary-material callers.
	public int addItem(Material material, int amount) {
		return addItem(new ItemStack(material), amount);
	}

	// Removes up to the requested amount. Returns the amount actually removed.
	public int removeItem(CargoItem item, int amount) {
		Integer current = items.get(item);
		if (current == null || amount <= 0) return 0;
		int toRemove = Math.min(amount, current);
		if (toRemove >= current) items.remove(item);
		else items.put(item, current - toRemove);
		return toRemove;
	}

	// Loads an item directly from storage, bypassing limit checks.
	public void load(CargoItem item, int amount) {
		if (item == null || amount <= 0) return;
		items.merge(item, amount, Integer::sum);
	}

	public void load(Material material, int amount) {
		CargoItem.fromItem(new ItemStack(material)).ifPresent(item -> load(item, amount));
	}

	// Removes all items (limits are untouched).
	public void clear() {
		items.clear();
	}

	public double getSellValue(CargoItem item) {
		return item.getEffectivePrice() * getCount(item);
	}

	public double getTotalSellValue() {
		double total = 0;
		for (Map.Entry<CargoItem, Integer> ent : items.entrySet()) {
			total += ent.getKey().getEffectivePrice() * ent.getValue();
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
